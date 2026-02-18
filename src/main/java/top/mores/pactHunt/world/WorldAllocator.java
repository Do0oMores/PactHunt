package top.mores.pactHunt.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import top.mores.pactHunt.PactHunt;

import java.util.*;

public class WorldAllocator {
    private final PactHunt plugin;
    private final Deque<String> freeWorlds = new ArrayDeque<>();
    private final Set<String> inUse = new HashSet<>();

    public WorldAllocator(PactHunt plugin) {
        this.plugin = plugin;

        List<String> instances = plugin.getConfig().getStringList("world-pool.instance");
        freeWorlds.addAll(instances);

        for (String worldName : instances) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World not loaded:" + worldName);
            }
        }
    }

    public String acquireWorld() {
        String world = freeWorlds.pollFirst();
        if (world == null) return null;
        inUse.add(world);
        return world;
    }

    public void releaseWorld(String worldName) {
        if (worldName == null) return;
        if (inUse.remove(worldName)) {
            freeWorlds.addLast(worldName);
        }
    }
}
