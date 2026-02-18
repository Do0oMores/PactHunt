package top.mores.pactHunt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mores.pactHunt.FileUtils.ChatColorUtil;
import top.mores.pactHunt.FileUtils.MessageLang;
import top.mores.pactHunt.match.LeaveReason;
import top.mores.pactHunt.match.MatchManager;
import top.mores.pactHunt.snapshot.SnapshotService;

public class MatchPlayerListener implements Listener {
    private final MatchManager matchManager;
    private final SnapshotService snapshotService;
    MessageLang messageLang=new MessageLang();

    public MatchPlayerListener(MatchManager matchManager, SnapshotService snapshotService) {
        this.matchManager = matchManager;
        this.snapshotService = snapshotService;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (!matchManager.isInMatch(p)) return;

        p.sendMessage(ChatColorUtil.color(messageLang.getDeathMessage()));
        matchManager.onDeath(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!matchManager.isInMatch(p)) return;

        matchManager.leave(p, LeaveReason.QUIT);
    }
}
