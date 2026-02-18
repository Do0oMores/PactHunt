package top.mores.pactHunt.snapshot;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import top.mores.pactHunt.PactHunt;

import java.util.*;

public class SnapshotService {
    private final PactHunt plugin;
    private final Map<UUID, PlayerSnapshot> snapshots = new HashMap<>();

    public SnapshotService(PactHunt plugin) {
        this.plugin = plugin;
    }

    public void saveSnapshot(Player p) {
        Collection<PotionEffect> effects = new ArrayList<>(p.getActivePotionEffects());
        PlayerSnapshot snap = new PlayerSnapshot(
                p.getUniqueId(),
                p.getLocation().clone(),
                p.getInventory().getContents().clone(),
                p.getInventory().getArmorContents().clone(),
                p.getExp(),
                p.getLevel(),
                Math.max(0.0, p.getHealth()),
                p.getFoodLevel(),
                p.getGameMode(),
                effects
        );
        snapshots.put(p.getUniqueId(), snap);
    }

    public void restoreSnapshot(Player p) {
        PlayerSnapshot snap = snapshots.remove(p.getUniqueId());
        if (snap == null) return;

        // 清空当前状态
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);

        // 恢复
        p.getInventory().setContents(snap.contents);
        p.getInventory().setArmorContents(snap.armor);
        p.setExp(snap.exp);
        p.setLevel(snap.level);
        p.setFoodLevel(snap.food);
        p.setGameMode(snap.gameMode);

        for (PotionEffect e : p.getActivePotionEffects()) {
            p.removePotionEffect(e.getType());
        }
        for (PotionEffect e : snap.effects) {
            p.addPotionEffect(e);
        }

        double max = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, snap.health));
    }
}
