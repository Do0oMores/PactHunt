package top.mores.pactHunt.FileUtils;

import org.bukkit.configuration.file.FileConfiguration;
import top.mores.pactHunt.PactHunt;

import java.util.List;

public class ConfigUtil {
    private FileConfiguration getConfig() {
        return PactHunt.getInstance().getConfigFile();
    }

    public int getMinPlayers() {
        return getConfig().getInt("match.min-players", 1);
    }

    public int getMaxPlayers() {
        return getConfig().getInt("match.max-players", 12);
    }

    public List<String> getBlockCommandsList(){
        return getConfig().getStringList("rules.block-commands");
    }
}
