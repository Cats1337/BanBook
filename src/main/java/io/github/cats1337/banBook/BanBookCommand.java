package io.github.cats1337.banBook;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BanBookCommand extends BanBookTabCompletion implements CommandExecutor {
    private final BanBook plugin;

    public BanBookCommand(BanBook plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage:" +
                    "\n§c/BanBook get - Gets a BanBook" +
                    "\n§c/BanBook give <player> - Gives a BanBook to a player" +
                    "\n§c/BanBook reload - Reloads the config");
            return true;
        }

        String subCommand = args[0].toLowerCase(); // Case-insensitive handling

        switch (subCommand) {
            case "get":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cYou must be a player to use this command!");
                    return false;
                }
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), plugin.getBanBook());
                    player.sendMessage("§aYour inventory was full, so the BanBook was dropped on the ground!");
                } else {
                    player.getInventory().addItem(plugin.getBanBook());
                    player.sendMessage("§aYou have received a BanBook!");
                }
                break;

            case "give":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /BanBook give <player>");
                    return false;
                }
                Player target = sender.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer " + args[1] + " is not online or does not exist.");
                    return false;
                }
                if (target.getInventory().firstEmpty() == -1) {
                    target.getWorld().dropItem(target.getLocation(), plugin.getBanBook());
                    sender.sendMessage("§a" + target.getName() + "'s inventory was full, so the BanBook was dropped on the ground!");
                } else {
                    target.getInventory().addItem(plugin.getBanBook());
                    sender.sendMessage("§aYou have given a BanBook to " + target.getName());
                }
                break;

            case "remove":
                if (!sender.hasPermission("banbook.remove")) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return false;
                }

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /banbook remove <item>");
                    return false;
                }

                String itemName = args[1].toLowerCase().replace(" ", "_");
                Material material = Material.matchMaterial(itemName);

                if (material == null) {
                    sender.sendMessage("§cInvalid item name: " + args[1]);
                    return false;
                }

                String namespacedId = material.getKey().toString(); // Example: minecraft.dirt

                if (plugin.getConfig().contains("bannedItems." + namespacedId)) {
                    plugin.getConfig().set("bannedItems." + namespacedId, null); // Remove entry
                    plugin.saveConfig();
                    sender.sendMessage("§aItem unbanned: " + namespacedId);
                } else {
                    sender.sendMessage("§cItem is not banned: " + namespacedId);
                }
                break;


            case "reload":
                if (!sender.hasPermission("banbook.reload")) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return false;
                }
                plugin.reloadConfig();
                sender.sendMessage("§aConfig reloaded!");
                break;

            default:
                sender.sendMessage("§cUsage:" +
                        "\n§c/BanBook get - Gets a BanBook" +
                        "\n§c/BanBook give <player> - Gives a BanBook to a player" +
                        "\n§c/BanBook reload - Reloads the config");
                break;
        }

        return true;
    }
}