package top.mores.pactHunt.FileUtils;

import org.bukkit.ChatColor;

import java.util.List;

public class ChatColorUtil {

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> list) {
        return list.stream()
                .map(ChatColorUtil::color)
                .toList();
    }
}
