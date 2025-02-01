package io.github.cats1337.banBook;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BanBookTabCompletion implements TabCompleter {
    private final JavaPlugin plugin;

    public BanBookTabCompletion(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Tab completion for the first argument
            return List.of("get", "give", "reload", "remove");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                // Tab completion for player names when using "give"
                String prefix = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("remove")) {
                // Tab completion for banned items when using "remove"
                String prefix = args[1].toLowerCase();
                Set<String> bannedItems = plugin.getConfig().getConfigurationSection("bannedItems") != null
                        ? plugin.getConfig().getConfigurationSection("bannedItems").getKeys(false)
                        : Set.of();

                return bannedItems.stream()
                        .filter(item -> item.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
        }
        return null; // No tab completion for other cases
    }
}
