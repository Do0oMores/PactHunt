package top.mores.pactHunt;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.mores.pactHunt.command.CommandTabUtil;
import top.mores.pactHunt.command.HuntCommand;
import top.mores.pactHunt.extract.ExtractionManager;
import top.mores.pactHunt.listener.ExtractionListener;
import top.mores.pactHunt.listener.MatchPlayerListener;
import top.mores.pactHunt.listener.MatchProtectionListener;
import top.mores.pactHunt.match.MatchManager;
import top.mores.pactHunt.snapshot.SnapshotService;
import top.mores.pactHunt.world.WorldAllocator;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class PactHunt extends JavaPlugin {

    private static PactHunt instance;
    private WorldAllocator worldAllocator;
    private SnapshotService snapshotService;
    private ExtractionManager extractionManager;
    private MatchManager matchManager;
    private FileConfiguration config;
    private FileConfiguration message;
    private File configFile;
    private File messageFile;

    @Override
    public void onEnable() {
        instance = this;
        initFiles();
        saveDefaultConfig();

        this.worldAllocator = new WorldAllocator(this);
        this.snapshotService = new SnapshotService(this);
        this.extractionManager = new ExtractionManager(this);
        this.matchManager = new MatchManager(this, worldAllocator, snapshotService, extractionManager);

        // listeners
        Bukkit.getPluginManager().registerEvents(new MatchProtectionListener(matchManager), this);
        Bukkit.getPluginManager().registerEvents(new MatchPlayerListener(matchManager, snapshotService), this);
        Bukkit.getPluginManager().registerEvents(new ExtractionListener(matchManager, extractionManager, this), this);

        Objects.requireNonNull(getCommand("hunt")).setExecutor(new HuntCommand(matchManager, this));
        Objects.requireNonNull(getCommand("hunt")).setTabCompleter(new CommandTabUtil());

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

    public void reloadConfigFile(){
        config= YamlConfiguration.loadConfiguration(configFile);
    }

    public @NotNull FileConfiguration getConfigFile(){
        if (config==null){
            reloadConfigFile();
        }
        return config;
    }

    public void saveMessageFile(){
        try{
            message.save(messageFile);
        }catch (IOException e){
            getLogger().severe("ERROR TO SAVE MESSAGE FILE"+e.getMessage());
        }
    }

    public void reloadMessage(){
        message=YamlConfiguration.loadConfiguration(messageFile);
    }

    public FileConfiguration getMessage(){
        if (message==null){
            reloadMessage();
        }
        return message;
    }

    private void initFiles(){
        configFile=new File(getDataFolder(),"config.yml");
        if (!configFile.exists()){
            boolean isCreateDir=configFile.getParentFile().mkdirs();
            if (!isCreateDir){
                getLogger().warning("FAIL TO CREATE config.yml DIR");
                return;
            }
            saveResource("config.yml",false);
        }
        reloadConfigFile();

        messageFile=new File(getDataFolder(),"lang.yml");
        if (!messageFile.exists()){
            try{
                saveResource("lang.yml",false);
            }catch (Exception e){
                getLogger().warning("FAIL TO CREATE lang.yml"+e.getMessage());
            }
        }
        reloadMessage();
    }
}
