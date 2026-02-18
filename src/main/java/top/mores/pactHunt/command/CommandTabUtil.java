package top.mores.pactHunt.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandTabUtil implements TabCompleter {

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("hunt")) {
            if (args.length == 1) {
                if (sender instanceof Player) {
                    completions.add("join");
                    completions.add("leave");
                }
                if (sender.isOp()) {
                    completions.add("start");
                    completions.add("end");
                    completions.add("reload");
                }
                return filterCompletions(completions, args[0]);
            }
        }
        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String arg) {
        List<String> result = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(arg.toLowerCase())) {
                result.add(completion);
            }
        }
        return result;
    }
}
