package com.example.shopplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private static ShopPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("ShopPlugin has been enabled!");

        // Register the /shop command
        ShopCommand shopCommand = new ShopCommand(this);
        getCommand("shop").setExecutor(shopCommand);
        getCommand("shop").setTabCompleter(shopCommand);

        // Register GUI listener
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("ShopPlugin has been disabled!");
    }

    public static ShopPlugin getInstance() {
        return instance;
    }
}
