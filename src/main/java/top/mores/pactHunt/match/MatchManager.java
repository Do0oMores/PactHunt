package top.mores.pactHunt.match;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.mores.pactHunt.PactHunt;
import top.mores.pactHunt.extract.ExtractionManager;
import top.mores.pactHunt.snapshot.SnapshotService;
import top.mores.pactHunt.world.WorldAllocator;

import java.util.*;

public class MatchManager {

    private final PactHunt plugin;
    private final WorldAllocator worldAllocator;
    private final SnapshotService snapshotService;
    private final ExtractionManager extractionManager;

    private final Map<UUID, UUID> playerToMatch = new HashMap<>();
    private final Map<UUID, Match> matches = new HashMap<>();

    public MatchManager(PactHunt plugin,
                        WorldAllocator worldAllocator,
                        SnapshotService snapshotService,
                        ExtractionManager extractionManager) {
        this.plugin = plugin;
        this.worldAllocator = worldAllocator;
        this.snapshotService = snapshotService;
        this.extractionManager = extractionManager;
    }

    public boolean isInMatch(Player player) {
        return playerToMatch.containsKey(player.getUniqueId());
    }

    public Match getMatchOf(Player player) {
        UUID mid = playerToMatch.get(player.getUniqueId());
        return mid == null ? null : matches.get(mid);
    }

    public MatchState getStateOf(Player player) {
        Match match = getMatchOf(player);
        return match == null ? null : match.getState();
    }

    public void join(Player player) {
        if (isInMatch(player)) {
            player.sendMessage("你已经在对局中");
            return;
        }
        Match match = findOrCreateWaitingMatch();
        if (match.getPlayers().size() >= getMaxPlayers()) {
            player.sendMessage("当前对局已满，稍后再试");
            return;
        }
        snapshotService.saveSnapshot(player);

        match.getPlayers().add(player.getUniqueId());
        match.getAlive().add(player.getUniqueId());
        match.getSessions().put(player.getUniqueId(), new PlayerSession(player.getUniqueId()));
        playerToMatch.put(player.getUniqueId(), match.getId());

        teleportToMatchSpawn(player, match);
        player.sendMessage("已加入对局" + match.getId().toString().substring(0, 8));

        if (match.getState() == MatchState.WAITING && match.getPlayers().size() >= getMinPlayers()) {
            startCountdown(match);
        }
    }

    public void leave(Player player, LeaveReason reason) {
        Match match = getMatchOf(player);
        if (match == null) {
            player.sendMessage("你不在任何对局中");
            return;
        }
        extractionManager.cancelExtraction(player, "离开对局");

        match.getPlayers().remove(player.getUniqueId());
        match.getAlive().remove(player.getUniqueId());
        match.getSessions().remove(player.getUniqueId());
        playerToMatch.remove(player.getUniqueId());

        snapshotService.restoreSnapshot(player);
        teleportToLobby(player);

        if (match.getState() == MatchState.STARTING && match.getPlayers().size() < getMinPlayers()) {
            cancelCountdown(match, "人数不足，倒计时已取消");
            match.setState(MatchState.WAITING);
        }

        if (match.getState() == MatchState.RUNNING) {
            checkEndCondition(match);
        }

        if ((match.getState() == MatchState.WAITING || match.getState() == MatchState.STARTING) && match.getPlayers().isEmpty()) {
            closeMatch(match);
        }
    }

    public void onDeath(Player player) {
        Match match = getMatchOf(player);
        if (match == null) return;

        match.getAlive().remove(player.getUniqueId());
        extractionManager.cancelExtraction(player, "你已死亡");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                leave(player, LeaveReason.DEATH);
                player.sendMessage("你已淘汰");
            }
        }, 1L);
        checkEndCondition(match);
    }

    public void onExtracted(Player player) {
        Match match = getMatchOf(player);
        if (match == null) return;

        PlayerSession playerSession = match.getSessions().get(player.getUniqueId());
        if (playerSession != null) playerSession.setExtracted(true);

        player.sendMessage("撤离成功");

        leave(player, LeaveReason.EXTRACTED);
        checkEndCondition(match);
    }

    public void adminStart(Player player) {
        Match match = getMatchOf(player);
        if (match == null) {
            player.sendMessage("你不在对局中，无法强制开启");
            return;
        }
        if (match.getState() == MatchState.RUNNING) {
            player.sendMessage("对局已开始");
            return;
        }
        startMatch(match);
    }

    public void adminEnd(Player player) {
        Match match = getMatchOf(player);
        if (match == null) {
            player.sendMessage("你不在对局中，无法强制结束");
            return;
        }
        endMatch(match, LeaveReason.ADMIN_END);
    }

    public void shutdownCleanup() {
        for (UUID playerUID : new ArrayList<>(playerToMatch.keySet())) {
            Player player = Bukkit.getPlayer(playerUID);
            if (player != null && player.isOnline()) {
                leave(player, LeaveReason.SHUTDOWN);
            }
        }
        for (Match match : new ArrayList<>(matches.values())) {
            closeMatch(match);
        }
    }

    private Match findOrCreateWaitingMatch() {
        for (Match match : matches.values()) {
            if (match.getState() == MatchState.WAITING) return match;
        }
        String worldName = worldAllocator.acquireWorld();
        if (worldName == null) {
            throw new IllegalStateException("没有可用的对局世界实例");
        }
        Match match = new Match(worldName);
        matches.put(match.getId(), match);

        applyWorldBorder(match);
        plugin.getLogger().info("Create match" + match.getId() + "in world" + worldName);
        return match;
    }

    private void startCountdown(Match match) {
        if (match.getState() != MatchState.WAITING) return;

        match.setState(MatchState.STARTING);
        int seconds = plugin.getConfig().getInt("match.start-countdown-seconds", 10);

        broadcastToMatch(match, "人数已满足，" + seconds + "秒后开始...");

        int taskId = new BukkitRunnable() {
            int left = seconds;

            @Override
            public void run() {
                if (match.getState() != MatchState.STARTING) {
                    cancel();
                    return;
                }
                if (match.getPlayers().size() < getMinPlayers()) {
                    cancelCountdown(match, "人数不足，倒计时取消");
                    match.setState(MatchState.WAITING);
                    cancel();
                    return;
                }
                if (left <= 0) {
                    cancel();
                    startMatch(match);
                    return;
                }
                if (left <= 5 || left % 5 == 0) {
                    broadcastToMatch(match, "对局将在" + left + "秒后开始");
                    forEachPlayer(match, player -> player.playSound(player.getLocation(),
                            Sound.UI_BUTTON_CLICK, 1f, 1f));
                }
                left--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        match.setCountdownTaskId(taskId);
    }

    private void startMatch(Match match) {
        if (match.getState() == MatchState.RUNNING) return;

        if (match.getCountdownTaskId() != -1) {
            Bukkit.getScheduler().cancelTask(match.getCountdownTaskId());
            match.setCountdownTaskId(-1);
        }
        match.setState(MatchState.RUNNING);
        match.setStartAt(System.currentTimeMillis());

        broadcastToMatch(match, "对局开始");
        extractionManager.prepareMatchExtraction(match);

        int maxDuration = plugin.getConfig().getInt("match.max-duration-seconds", 1500);
        int timeoutTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (match.getState() == MatchState.RUNNING) {
                    endMatch(match, LeaveReason.TIMEOUT);
                }
            }
        }.runTaskLater(plugin, maxDuration * 20L).getTaskId();
        match.setTimeoutTaskId(timeoutTaskId);
    }

    public void endMatch(Match match, LeaveReason reason) {
        if (match.getState() == MatchState.ENDING || match.getState() == MatchState.CLOSED) return;
        match.setState(MatchState.ENDING);

        if (match.getTimeoutTaskId() != -1) {
            Bukkit.getScheduler().cancelTask(match.getTimeoutTaskId());
            match.setTimeoutTaskId(-1);
        }
        broadcastToMatch(match, "对局结束" + reason);

        forEachPlayer(match, player -> {
            extractionManager.cancelExtraction(player, "对局结束");
            snapshotService.restoreSnapshot(player);
            teleportToLobby(player);
            playerToMatch.remove(player.getUniqueId());
        });
        closeMatch(match);
    }

    private void closeMatch(Match match) {
        match.setState(MatchState.CLOSED);
        matches.remove(match.getId());

        worldAllocator.releaseWorld(match.getWorldName());
        plugin.getLogger().info("Close match" + match.getId() + ", released world" + match.getWorldName());
    }

    private void checkEndCondition(Match match) {
        if (match.getState() != MatchState.RUNNING) return;

        if (match.getAlive().isEmpty()) {
            endMatch(match, LeaveReason.NO_ALIVE_PLAYERS);
        }
    }

    private void teleportToMatchSpawn(Player player, Match match) {
        Location spawn = readLocation("match-spawn");
        World world = Bukkit.getWorld(match.getWorldName());
        if (world == null) throw new IllegalStateException("Match world is not loaded" + match.getWorldName());
        spawn.setWorld(world);

        player.teleport(spawn);
        player.setGameMode(GameMode.SURVIVAL);
    }

    private void teleportToLobby(Player player) {
        Location lobby = readLocation("lobby-spawn");
        World world = Bukkit.getWorld(plugin.getConfig().getString("lobby-world", "lobby"));
        if (world != null) lobby.setWorld(world);

        player.teleport(lobby);
    }

    private Location readLocation(String path) {
        String worldName = plugin.getConfig().getString(path + ".world");
        double x = plugin.getConfig().getDouble(path + ".x");
        double y = plugin.getConfig().getDouble(path + ".y");
        double z = plugin.getConfig().getDouble(path + ".z");
        float yaw = ((float) plugin.getConfig().getDouble(path + "yaw"));
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch");

        World world = worldName == null ? null : Bukkit.getWorld(worldName);
        return new Location(world, x, y, z, yaw, pitch);
    }

    private void cancelCountdown(Match match, String msg) {
        if (match.getCountdownTaskId() != -1) {
            Bukkit.getScheduler().cancelTask(match.getCountdownTaskId());
            match.setCountdownTaskId(-1);
        }
        broadcastToMatch(match, msg);
    }

    private void applyWorldBorder(Match match) {
        if (!plugin.getConfig().getBoolean("border.enabled", true)) return;
        World world = Bukkit.getWorld(match.getWorldName());
        if (world == null) return;

        double cx = plugin.getConfig().getDouble("border-center-x", 0);
        double cz = plugin.getConfig().getDouble("border-center-z", 0);
        double radius = plugin.getConfig().getDouble("border.radius", 450);

        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(cx, cz);
        worldBorder.setSize(radius * 2);
        worldBorder.setWarningDistance(8);
    }

    private void forEachPlayer(Match match, java.util.function.Consumer<Player> c) {
        for (UUID uuid : match.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) c.accept(player);
        }
    }

    private void broadcastToMatch(Match match, String msg) {
        forEachPlayer(match, player -> player.sendMessage(msg));
    }

    private int getMinPlayers() {
        return plugin.getConfig().getInt("match.min-players", 1);
    }

    private int getMaxPlayers() {
        return plugin.getConfig().getInt("match.max-players", 12);
    }
}
