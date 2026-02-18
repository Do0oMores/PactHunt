package top.mores.pactHunt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import top.mores.pactHunt.PactHunt;
import top.mores.pactHunt.extract.ExtractionManager;
import top.mores.pactHunt.extract.ExtractionPoint;
import top.mores.pactHunt.match.Match;
import top.mores.pactHunt.match.MatchManager;
import top.mores.pactHunt.match.MatchState;

public class ExtractionListener implements Listener {
    private final MatchManager matchManager;
    private final ExtractionManager extractionManager;
    private final PactHunt plugin;

    public ExtractionListener(MatchManager matchManager, ExtractionManager extractionManager, PactHunt plugin) {
        this.matchManager = matchManager;
        this.extractionManager = extractionManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Match m = matchManager.getMatchOf(p);
        if (m == null) return;
        if (m.getState() != MatchState.RUNNING) return;

        if (extractionManager.isExtracting(p)) {
            boolean stillInside = false;
            for (ExtractionPoint pt : extractionManager.getPoints(m)) {
                if (pt.contains(p.getLocation())) {
                    stillInside = true;
                    break;
                }
            }
            if (!stillInside) {
                extractionManager.cancelExtraction(p, "离开撤离区域");
            }
            return;
        }

        for (ExtractionPoint pt : extractionManager.getPoints(m)) {
            if (pt.contains(p.getLocation())) {
                extractionManager.tryStartExtraction(p, () -> matchManager.onExtracted(p));
                break;
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        Match m = matchManager.getMatchOf(p);
        if (m == null) return;
        if (m.getState() != MatchState.RUNNING) return;

        if (plugin.getConfig().getBoolean("extraction.cancel-on-damage", true)) {
            if (extractionManager.isExtracting(p)) {
                extractionManager.cancelExtraction(p, "受到伤害");
            }
        }
    }
}
