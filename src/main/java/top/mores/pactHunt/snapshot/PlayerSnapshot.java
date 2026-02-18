package top.mores.pactHunt.snapshot;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.UUID;

public class PlayerSnapshot {
    public final UUID uuid;
    public final Location location;
    public final ItemStack[] contents;
    public final ItemStack[] armor;
    public final float exp;
    public final int level;
    public final double health;
    public final int food;
    public final GameMode gameMode;
    public final Collection<PotionEffect> effects;

    public PlayerSnapshot(UUID uuid,
                          Location location,
                          ItemStack[] contents,
                          ItemStack[] armor,
                          float exp,
                          int level,
                          double health,
                          int food,
                          GameMode gameMode,
                          Collection<PotionEffect> effects) {
        this.uuid = uuid;
        this.location = location;
        this.contents = contents;
        this.armor = armor;
        this.exp = exp;
        this.level = level;
        this.health = health;
        this.food = food;
        this.gameMode = gameMode;
        this.effects = effects;
    }
}
