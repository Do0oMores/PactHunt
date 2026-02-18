package top.mores.pactHunt.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mores.pactHunt.PactHunt;
import top.mores.pactHunt.match.LeaveReason;
import top.mores.pactHunt.match.MatchManager;

public class HuntCommand implements CommandExecutor {
    private final MatchManager matchManager;
    private final PactHunt plugin;

    public HuntCommand(MatchManager matchManager, PactHunt plugin) {
        this.matchManager = matchManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players.");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(ChatColor.YELLOW + "/hunt join | /hunt leave | /hunt start | /hunt end");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join" -> matchManager.join(p);
            case "leave" -> matchManager.leave(p, LeaveReason.COMMAND_LEAVE);
            case "start" -> {
                if (!p.hasPermission("pacthunt.admin")) {
                    p.sendMessage(ChatColor.RED + "没有权限。");
                    return true;
                }
                matchManager.adminStart(p);
            }
            case "end" -> {
                if (!p.hasPermission("pacthunt.admin")) {
                    p.sendMessage(ChatColor.RED + "没有权限。");
                    return true;
                }
                matchManager.adminEnd(p);
            }
            default -> p.sendMessage(ChatColor.YELLOW + "/hunt join | /hunt leave | /hunt start | /hunt end");
        }

        return true;
    }
}
