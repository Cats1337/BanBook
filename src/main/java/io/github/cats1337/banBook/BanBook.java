package io.github.cats1337.banBook;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@Getter
public class BanBook extends JavaPlugin {
    public final String noteTitle = "§4§lBan Book";
    public final String noteLore = "§7§oA powerful book of item banishment.";
    public static final String notePages = "      §4§lBan Book\n" +
            "\n" +
            "§8Write the name of the item you wish to ban.\n" +
            "§8ie. 'diamond_sword' or 'dirt'. No quotes, case-insensitive.\n" +
            "\n" +
            "\n" +
            "§0Type on the line:\n" +
            "\nItem:§c " +
            "\n§0-------------------";

    @Override
    public void onEnable() {
        Bukkit.getServer().getConsoleSender().sendMessage("§9    ╱|、\n§9   (§b` -§9 7   §3" + getDescription().getName() + " §8- §4BanBook\n§9   |、˜〵    §1v" + getDescription().getVersion() + "\n§9   じしˍ,)ノ");

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BanBookListener(this), this);

        getCommand("BanBook").setExecutor(new BanBookCommand(this));
        getCommand("BanBook").setTabCompleter(new BanBookTabCompletion(this));

    }

    public ItemStack getBanBook() {
        ItemStack note = new ItemStack(Material.WRITABLE_BOOK, 1);
        BookMeta noteMeta = (BookMeta)note.getItemMeta();

        noteMeta.setEnchantmentGlintOverride(true);
        noteMeta.setFireResistant(true);
        noteMeta.setItemName(noteTitle);
        noteMeta.setRarity(ItemRarity.EPIC);
        noteMeta.setLore(List.of(noteLore));
        noteMeta.setTitle("BanBook");
        noteMeta.setPages(notePages);
        noteMeta.setGeneration(BookMeta.Generation.TATTERED);

        note.setItemMeta(noteMeta);

        return note;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(getDescription().getName() + " V: " + getDescription().getVersion() + " has been disabled");
    }
}
