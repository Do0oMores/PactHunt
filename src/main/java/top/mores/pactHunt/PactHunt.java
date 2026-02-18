package top.mores.pactHunt;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import top.mores.pactHunt.command.HuntCommand;
import top.mores.pactHunt.extract.ExtractionManager;
import top.mores.pactHunt.listener.ExtractionListener;
import top.mores.pactHunt.listener.MatchPlayerListener;
import top.mores.pactHunt.listener.MatchProtectionListener;
import top.mores.pactHunt.match.MatchManager;
import top.mores.pactHunt.snapshot.SnapshotService;
import top.mores.pactHunt.world.WorldAllocator;

public final class PactHunt extends JavaPlugin {

    private static PactHunt instance;
    private WorldAllocator worldAllocator;
    private SnapshotService snapshotService;
    private ExtractionManager extractionManager;
    private MatchManager matchManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.worldAllocator = new WorldAllocator(this);
        this.snapshotService = new SnapshotService(this);
        this.extractionManager = new ExtractionManager(this);
        this.matchManager = new MatchManager(this, worldAllocator, snapshotService, extractionManager);

        // listeners
        Bukkit.getPluginManager().registerEvents(new MatchProtectionListener(matchManager, this), this);
        Bukkit.getPluginManager().registerEvents(new MatchPlayerListener(matchManager, snapshotService, this), this);
        Bukkit.getPluginManager().registerEvents(new ExtractionListener(matchManager, extractionManager, this), this);

        getCommand("hunt").setExecutor(new HuntCommand(matchManager, this));

        getLogger().info("PactHunt enabled.");
    }

    @Override
    public void onDisable() {
        matchManager.shutdownCleanup();
        getLogger().info("Disabled");
    }

    public static PactHunt getInstance() {
        return instance;
    }
}
