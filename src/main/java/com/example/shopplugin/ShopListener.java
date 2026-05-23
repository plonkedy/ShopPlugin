package com.example.shopplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShopListener implements Listener {

    private final ShopPlugin plugin;

    public ShopListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        String title = event.getView().getTitle();
        if (!title.contains("Shop")) return;

        // Always cancel so players can't take items from the GUI
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) return;

        Inventory topInv = event.getView().getTopInventory();

        // Parse current page from title  e.g. "Page 2/3"
        int currentPage = parsePageFromTitle(title);

        // --- Navigation buttons ---
        if (slot == ShopGUI.getCloseSlot()) {
            player.closeInventory();
            return;
        }

        if (slot == ShopGUI.getPrevSlot() && currentPage > 0) {
            player.openInventory(ShopGUI.buildPage(currentPage - 1));
            return;
        }

        if (slot == ShopGUI.getNextSlot() && currentPage < ShopGUI.getTotalPages() - 1) {
            player.openInventory(ShopGUI.buildPage(currentPage + 1));
            return;
        }

        // --- Item slots (0-35) ---
        if (slot >= ShopGUI.getItemsPerPage()) return;

        ItemStack clicked = topInv.getItem(slot);
        if (clicked == null || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        int itemIndex = currentPage * ShopGUI.getItemsPerPage() + slot;
        List<ShopItem> items = ShopGUI.getShopItems();
        if (itemIndex >= items.size()) return;

        ShopItem shopItem = items.get(itemIndex);

        boolean isRightClick = event.isRightClick();

        if (!isRightClick) {
            // BUY
            handleBuy(player, shopItem);
        } else {
            // SELL
            handleSell(player, shopItem);
        }
    }

    // ---------------------------------------------------------------
    // Buy logic
    // NOTE: No economy plugin is hooked up by default — this uses a
    //       simple balance stored per-player on the server.
    //       Hook in Vault + an economy plugin for a real economy.
    // ---------------------------------------------------------------
    private void handleBuy(Player player, ShopItem shopItem) {
        // --- Vault / economy hook-in point ---
        // If you have Vault, replace this block:
        //   Economy eco = ...; // obtain from ServicesManager
        //   if (!eco.has(player, shopItem.getBuyPrice())) { ... tell player ... return; }
        //   eco.withdrawPlayer(player, shopItem.getBuyPrice());

        // For now we just give the item for free (demo mode)
        ItemStack reward = new ItemStack(shopItem.getMaterial(), shopItem.getAmount());
        player.getInventory().addItem(reward).forEach((k, leftover) ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover));

        player.sendMessage(ChatColor.GREEN + "✔ Bought " + shopItem.getAmount()
                + "x " + shopItem.getName()
                + ChatColor.GRAY + " for " + ChatColor.WHITE + "$" + shopItem.getBuyPrice()
                + ChatColor.GRAY + " (demo — no economy deducted)");
    }

    // ---------------------------------------------------------------
    // Sell logic
    // ---------------------------------------------------------------
    private void handleSell(Player player, ShopItem shopItem) {
        ItemStack toSell = new ItemStack(shopItem.getMaterial(), shopItem.getAmount());
        if (!player.getInventory().containsAtLeast(toSell, shopItem.getAmount())) {
            player.sendMessage(ChatColor.RED + "✘ You don't have enough "
                    + shopItem.getName() + " to sell!");
            return;
        }

        // Remove items from inventory
        player.getInventory().removeItem(toSell);

        // --- Vault / economy hook-in point ---
        // eco.depositPlayer(player, shopItem.getSellPrice());

        player.sendMessage(ChatColor.GOLD + "✔ Sold " + shopItem.getAmount()
                + "x " + shopItem.getName()
                + ChatColor.GRAY + " for " + ChatColor.WHITE + "$" + shopItem.getSellPrice()
                + ChatColor.GRAY + " (demo — no economy credited)");
    }

    // ---------------------------------------------------------------
    // Parse "Page X/Y" from title
    // ---------------------------------------------------------------
    private int parsePageFromTitle(String title) {
        try {
            // Title format: "... (Page X/Y)"
            int parenOpen  = title.lastIndexOf('(');
            int slash      = title.lastIndexOf('/');
            if (parenOpen < 0 || slash < 0) return 0;
            String num = title.substring(parenOpen + 1, slash).replaceAll("[^0-9]", "").trim();
            return Integer.parseInt(num) - 1; // 0-indexed
        } catch (Exception e) {
            return 0;
        }
    }
}
