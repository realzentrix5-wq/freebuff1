package com.example.freebuff1;

import org.bukkit.plugin.java.JavaPlugin;

public final class Freebuff1Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("freebuff1 enabled");

        if (getCommand("freebuff1") != null) {
            getCommand("freebuff1").setExecutor((sender, command, label, args) -> {
                sender.sendMessage("freebuff1 is running");
                return true;
            });
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("freebuff1 disabled");
    }
}
