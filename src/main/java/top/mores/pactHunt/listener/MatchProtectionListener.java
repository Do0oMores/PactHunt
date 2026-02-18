package top.mores.pactHunt.listener;

import org.bukkit.ChatColor;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import top.mores.pactHunt.FileUtils.ChatColorUtil;
import top.mores.pactHunt.FileUtils.ConfigUtil;
import top.mores.pactHunt.FileUtils.MessageLang;
import top.mores.pactHunt.match.Match;
import top.mores.pactHunt.match.MatchManager;
import top.mores.pactHunt.match.MatchState;

import java.util.List;

public class MatchProtectionListener implements Listener {
    private final MatchManager matchManager;
    MessageLang messageLang = new MessageLang();
    ConfigUtil configUtil=new ConfigUtil();

    public MatchProtectionListener(MatchManager matchManager) {
        this.matchManager = matchManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Match m = matchManager.getMatchOf(p);
        if (m == null) return;

        if (m.getState() == MatchState.STARTING) {
            if (e.getFrom().getX() != e.getTo().getX()
                    || e.getFrom().getY() != e.getTo().getY()
                    || e.getFrom().getZ() != e.getTo().getZ()) {
                e.setTo(e.getFrom());
            }
            return;
        }

        if (m.getState() == MatchState.RUNNING) {
            WorldBorder wb = p.getWorld().getWorldBorder();
            if (!wb.isInside(e.getTo())) {
                p.sendMessage(ChatColor.RED + "你正在越界！");
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        Match m = matchManager.getMatchOf(p);
        if (m == null) return;

        List<String> blocked = configUtil.getBlockCommandsList();
        String msg = e.getMessage().toLowerCase();

        for (String b : blocked) {
            if (msg.startsWith(b.toLowerCase())) {
                e.setCancelled(true);
                p.sendMessage(ChatColorUtil.color(messageLang.getBlockCommandsMessage()));
                return;
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        Match m = matchManager.getMatchOf(p);
        if (m == null) return;

        switch (e.getCause()) {
            case COMMAND, PLUGIN, SPECTATE, UNKNOWN -> {
            }
            default -> {
            }
        }
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "对局中禁止传送。");
        }
    }
}
