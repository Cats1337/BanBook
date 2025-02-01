package io.github.cats1337.banBook;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BanBookListener implements Listener {

    private final BanBook plugin;

    public BanBookListener(BanBook plugin) {
        this.plugin = plugin;
    }

    private boolean isBanned(String itemName) {
        return plugin.getConfig().getBoolean("bannedItems.minecraft." + itemName.toLowerCase(), false);

    }

    @EventHandler
    public void onBookEditEvent(PlayerEditBookEvent e) {
        if (e.getPreviousBookMeta().getItemName().equals(plugin.noteTitle) &&
                e.getPreviousBookMeta().getLore().get(0).equals(plugin.noteLore)) {

            // Only act if the player is signing the book
            if (e.isSigning()) {

                // Check if player has permission to use the BanBook
                if (e.getPlayer().hasPermission("banbook.use")) {
                    List<String> pages = e.getNewBookMeta().getPages();
                    List<String> targets = new ArrayList<>();

                    // Process each page of the book to extract item names
                    for (String page : pages) {
                        for (String line : page.split("\n")) {
                            if (line.startsWith("Item:")) {
                                // Extract the item name after "Item:"
                                String itemName = line.substring(7).trim();  // Removing "Item:" and trimming whitespace

                                // Strip numbers and symbols other than underscores
                                String cleanedItemName = itemName.replaceAll("[^a-zA-Z_]", "");

                                // Check if the cleaned name is a valid item name (length between 3 and 16)
                                if (Pattern.matches("^[a-zA-Z_]{3,16}$", cleanedItemName)) {
                                    targets.add(cleanedItemName);
                                }
                            }
                        }
                    }

                    // Ban the items if any valid targets are found
                    if (!targets.isEmpty()) {
                        banItems(targets);

                        // Notify all players about banned items
                        notifyPlayersAboutBannedItems(e.getPlayer(), targets);
                        // Remove the BanBook from the player's inventory
                        removeBanBookFromInventory(e);
                        e.getPlayer().playSound(e.getPlayer().getLocation(), "entity.item.break", 1, 1);
                    }
                }
                e.setSigning(false);
            }
        }
    }

    // Helper method to ban the items by adding them to the config
    private void banItems(List<String> targets) {
        for (String target : targets) {
            Material material = Material.matchMaterial(target);
            if (material != null) {
                plugin.getConfig().set("bannedItems.minecraft." + target, true);
            }
        }
        plugin.saveConfig();
    }

    // Helper method to notify all players about banned items
    private void notifyPlayersAboutBannedItems(Player player, List<String> targets) {
        for (Player p : player.getWorld().getPlayers()) {
            for (String target : targets) {
                String formattedTarget = formatItemName(target);
                p.sendTitle("§cItem Banned:", "§4" + formattedTarget, 10, 70, 20);
                p.sendMessage("§c" + formattedTarget + " has been banned by.");
            }
        }
    }

    public String formatItemName(String itemName) {
        // Split the item name by underscores
        String[] words = itemName.split("_");

        // Capitalize each word and join them back with spaces
        String formattedName = Stream.of(words)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));

        return formattedName;
    }


    // Helper method to remove the BanBook from the player's inventory
    private void removeBanBookFromInventory(PlayerEditBookEvent e) {
        Arrays.stream(e.getPlayer().getInventory().getContents())
                .filter(item -> item != null && item.getItemMeta() != null &&
                        item.getItemMeta().getItemName().equals(plugin.noteTitle) &&
                        item.getItemMeta().getLore().get(0).equals(plugin.noteLore))
                .forEach(item -> e.getPlayer().getInventory().remove(item));
    }


    // Additional event handlers for removing banned items
    // (e.g., PlayerInteractEvent, InventoryClickEvent, etc.)
    // These will enforce the ban by removing banned items from inventories.
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (item != null && isBanned(item.getType().name())) {
            if (p.hasPermission("banbook.bypass") || p.getGameMode() == GameMode.CREATIVE) {
                p.sendMessage("§aBypassing ban for " + item.getType().name());
                return; // Allow bypass for players with permission or in creative mode
            }

            p.getInventory().remove(item);
            p.sendMessage("§cA banned item has been removed from your inventory.");
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getInventory() instanceof Chest) {
            for (ItemStack item : e.getInventory().getContents()) {
                if (item != null && isBanned(item.getType().name())) {
                    e.getInventory().remove(item);
                }
            }
        }
        // Check if the player has any banned items in their inventory
        Player p = (Player) e.getPlayer();
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && isBanned(item.getType().name())) {
                if (p.hasPermission("banbook.bypass") || p.getGameMode() == GameMode.CREATIVE) {
                    p.sendMessage("§aBypassing ban for " + item.getType().name());
                    continue; // Allow bypass for players with permission or in creative mode
                }

                p.getInventory().remove(item);
                p.sendMessage("§cA banned item has been removed from your inventory.");
            }
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
//        get material name and check if it is banned

        if (isBanned(item.getType().name())) {
            if (e.getPlayer().hasPermission("banbook.bypass") || e.getPlayer().getGameMode() == GameMode.CREATIVE) {
                e.getPlayer().sendMessage("§aBypassing ban for " + item.getType().name());
                return; // Allow bypass for players with permission or in creative mode
            }

            e.setCancelled(true);
            e.getItem().remove();
            e.getPlayer().sendMessage("§cThis item is banned and cannot be picked up.");
        }
    }

}