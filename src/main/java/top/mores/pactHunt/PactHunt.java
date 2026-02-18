package top.mores.pactHunt;

import org.bukkit.plugin.java.JavaPlugin;

public final class PactHunt extends JavaPlugin {

    private static PactHunt instance;

    @Override
    public void onEnable() {
        instance=this;
    }

    @Override
    public void onDisable() {

    }

    public static PactHunt getInstance(){
        return instance;
    }
}
