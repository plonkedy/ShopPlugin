package com.example.shopplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopGUI {

    // ---------------------------------------------------------------
    // Define your shop items here — add or remove as you like!
    // ---------------------------------------------------------------
    private static final List<ShopItem> SHOP_ITEMS = Arrays.asList(
        new ShopItem("Diamond",       Material.DIAMOND,        100.0, 50.0,  1),
        new ShopItem("Iron Ingot",    Material.IRON_INGOT,     10.0,  5.0,   1),
        new ShopItem("Gold Ingot",    Material.GOLD_INGOT,     25.0,  12.0,  1),
        new ShopItem("Coal",          Material.COAL,           3.0,   1.0,   1),
        new ShopItem("Oak Log",       Material.OAK_LOG,        5.0,   2.0,   4),
        new ShopItem("Wheat",         Material.WHEAT,          2.0,   1.0,   8),
        new ShopItem("Bread",         Material.BREAD,          6.0,   3.0,   4),
        new ShopItem("Apple",         Material.APPLE,          4.0,   2.0,   4),
        new ShopItem("Cooked Beef",   Material.COOKED_BEEF,    8.0,   4.0,   4),
        new ShopItem("Emerald",       Material.EMERALD,        75.0,  35.0,  1),
        new ShopItem("Redstone",      Material.REDSTONE,       5.0,   2.0,   4),
        new ShopItem("Lapis Lazuli",  Material.LAPIS_LAZULI,   8.0,   4.0,   4),
        new ShopItem("Sand",          Material.SAND,           1.0,   0.5,   8),
        new ShopItem("Stone",         Material.STONE,          1.0,   0.5,   8),
        new ShopItem("Obsidian",      Material.OBSIDIAN,       20.0,  10.0,  1),
        new ShopItem("Blaze Rod",     Material.BLAZE_ROD,      30.0,  15.0,  1),
        new ShopItem("Ender Pearl",   Material.ENDER_PEARL,    25.0,  12.0,  1),
        new ShopItem("Nether Brick",  Material.NETHER_BRICK,   5.0,   2.0,   4)
    );

    // ---------------------------------------------------------------
    // Pages
    // ---------------------------------------------------------------
    private static final int ITEMS_PER_PAGE = 36; // 4 rows for items, 1 row for controls
    private static final int GUI_SIZE       = 45; // 5 rows total

    private static final int PREV_SLOT = 39;
    private static final int INFO_SLOT = 40;
    private static final int NEXT_SLOT = 41;
    private static final int CLOSE_SLOT = 44;

    public static int getTotalPages() {
        return (int) Math.ceil((double) SHOP_ITEMS.size() / ITEMS_PER_PAGE);
    }

    public static Inventory buildPage(int page) {
        int totalPages = getTotalPages();
        String title = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "✦ Shop" +
                ChatColor.GRAY + " (Page " + (page + 1) + "/" + totalPages + ")";
        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, title);

        // Fill item slots
        int start = page * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, SHOP_ITEMS.size());

        for (int i = start; i < end; i++) {
            ShopItem si = SHOP_ITEMS.get(i);
            inv.setItem(i - start, buildItemStack(si));
        }

        // Fill empty item slots with glass panes
        ItemStack filler = buildFiller();
        for (int slot = 0; slot < ITEMS_PER_PAGE; slot++) {
            if (inv.getItem(slot) == null) inv.setItem(slot, filler);
        }

        // Bottom bar — always fill the whole row first
        ItemStack barFiller = buildFiller();
        for (int slot = 36; slot < 45; slot++) inv.setItem(slot, barFiller);

        // Navigation buttons
        if (page > 0)             inv.setItem(PREV_SLOT, buildNavButton(Material.ARROW,        ChatColor.YELLOW + "◀ Previous Page"));
        if (page < totalPages - 1) inv.setItem(NEXT_SLOT, buildNavButton(Material.ARROW,        ChatColor.YELLOW + "Next Page ▶"));
        inv.setItem(INFO_SLOT,   buildNavButton(Material.BOOK,
                ChatColor.AQUA + "Left-click" + ChatColor.GRAY + " = Buy  |  " +
                ChatColor.GOLD + "Right-click" + ChatColor.GRAY + " = Sell"));
        inv.setItem(CLOSE_SLOT,  buildNavButton(Material.BARRIER, ChatColor.RED + "Close Shop"));

        return inv;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private static ItemStack buildItemStack(ShopItem si) {
        ItemStack stack = new ItemStack(si.getMaterial(), si.getAmount());
        ItemMeta meta  = stack.getItemMeta();
        if (meta == null) return stack;

        meta.setDisplayName(ChatColor.GREEN + si.getName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Amount: " + ChatColor.WHITE + si.getAmount());
        lore.add("");
        lore.add(ChatColor.AQUA + "Buy:  " + ChatColor.WHITE + "$" + formatPrice(si.getBuyPrice()));
        lore.add(ChatColor.GOLD + "Sell: " + ChatColor.WHITE + "$" + formatPrice(si.getSellPrice()));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Left-click"  + ChatColor.GRAY + " to buy");
        lore.add(ChatColor.YELLOW + "Right-click" + ChatColor.GRAY + " to sell");

        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack buildFiller() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta  = pane.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); pane.setItemMeta(meta); }
        return pane;
    }

    private static ItemStack buildNavButton(Material mat, String name) {
        ItemStack btn = new ItemStack(mat);
        ItemMeta meta = btn.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); btn.setItemMeta(meta); }
        return btn;
    }

    private static String formatPrice(double price) {
        if (price == Math.floor(price)) return String.valueOf((int) price);
        return String.format("%.2f", price);
    }

    // ---------------------------------------------------------------
    // Getters for other classes
    // ---------------------------------------------------------------
    public static List<ShopItem> getShopItems()  { return SHOP_ITEMS; }
    public static int getItemsPerPage()           { return ITEMS_PER_PAGE; }
    public static int getPrevSlot()               { return PREV_SLOT; }
    public static int getNextSlot()               { return NEXT_SLOT; }
    public static int getCloseSlot()              { return CLOSE_SLOT; }

    /** Opens the shop at page 0 for the given player. */
    public static void open(Player player) {
        player.openInventory(buildPage(0));
    }
}
