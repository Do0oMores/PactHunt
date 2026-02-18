package top.mores.pactHunt.FileUtils;

import org.bukkit.configuration.file.FileConfiguration;
import top.mores.pactHunt.PactHunt;

public class MessageLang {
    private FileConfiguration getMessage() {
        return PactHunt.getInstance().getMessage();
    }

    public String getDeathMessage() {
        return getMessage().getString("death-message");
    }

    public String getBlockCommandsMessage() {
        return getMessage().getString("block-commands-tip");
    }

    public String getNoPermissionUseCommand(){
        return getMessage().getString("no-permission-use-command");
    }

    public String getReloadConfigTip(){
        return getMessage().getString("reload-config-tip");
    }

    public String getCommandTip(){
        return getMessage().getString("command-tip");
    }

    public String getAlreadyInMatch(){
        return getMessage().getString("already-in-match");
    }

    public String getFullMatchTip(){
        return getMessage().getString("full-match-tip");
    }

    public String getNoneInMatchTip(){
        return getMessage().getString("none-in-match-tip");
    }
}
