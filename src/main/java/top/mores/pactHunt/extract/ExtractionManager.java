package top.mores.pactHunt.extract;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.mores.pactHunt.PactHunt;
import top.mores.pactHunt.match.Match;

import java.util.*;

public class ExtractionManager {
    private final PactHunt plugin;

    // matchId -> points
    private final Map<UUID, List<ExtractionPoint>> matchPoints = new HashMap<>();
    // player -> taskId
    private final Map<UUID, Integer> extractingTask = new HashMap<>();

    public ExtractionManager(PactHunt plugin) {
        this.plugin = plugin;
    }

    public List<ExtractionPoint> getPoints(Match match) {
        return matchPoints.getOrDefault(match.getId(), Collections.emptyList());
    }

    public void prepareMatchExtraction(Match match) {
        // 读取配置 points 并把 world 替换为 match.worldName
        List<Map<?, ?>> pointsCfg = plugin.getConfig().getMapList("extraction.points");
        List<ExtractionPoint> points = new ArrayList<>();

        for (Map<?, ?> m : pointsCfg) {
            String id = String.valueOf(m.get("id"));
            double x = Double.parseDouble(String.valueOf(m.get("x")));
            double y = Double.parseDouble(String.valueOf(m.get("y")));
            double z = Double.parseDouble(String.valueOf(m.get("z")));
            double radius = parseDoubleSafe((Map<String, Object>) m, "radius", 4.0);

            World w = Bukkit.getWorld(match.getWorldName());
            if (w == null) continue;

            Location center = new Location(w, x, y, z);
            points.add(new ExtractionPoint(id, center, radius));
        }

        matchPoints.put(match.getId(), points);
    }

    public void clearMatch(Match match) {
        matchPoints.remove(match.getId());
    }

    public boolean isExtracting(Player p) {
        return extractingTask.containsKey(p.getUniqueId());
    }

    public void tryStartExtraction(Player p, Runnable onSuccess) {
        if (isExtracting(p)) return;

        int channelSeconds = plugin.getConfig().getInt("extraction.channel-seconds", 12);
        int taskId = new BukkitRunnable() {
            int leftTicks = channelSeconds * 20;

            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancelExtraction(p, "断线");
                    cancel();
                    return;
                }
                leftTicks -= 1;

                if (leftTicks % 20 == 0) {
                    int leftSec = Math.max(0, leftTicks / 20);
                    p.sendMessage(ChatColor.AQUA + "撤离中… 剩余 " + leftSec + " 秒（受击/离开将中断）");
                }

                if (leftTicks <= 0) {
                    extractingTask.remove(p.getUniqueId());
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                    onSuccess.run();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L).getTaskId();

        extractingTask.put(p.getUniqueId(), taskId);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
    }

    public void cancelExtraction(Player p, String reason) {
        Integer tid = extractingTask.remove(p.getUniqueId());
        if (tid != null) {
            Bukkit.getScheduler().cancelTask(tid);
            if (p.isOnline()) {
                p.sendMessage(ChatColor.RED + "撤离已中断：" + reason);
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
            }
        }
    }

    public static double parseDoubleSafe(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.getOrDefault(key, defaultValue);
        if (value == null) return defaultValue;

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
