package com.ashkiano.automonthlyrewards;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;

public class AutoMonthlyRewards extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        Metrics metrics = new Metrics(this,21258);
        this.getLogger().info("Thank you for using the AutoMonthlyRewards plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            public void run() {
                String playerName = event.getPlayer().getName();
                Calendar now = Calendar.getInstance();
                int currentMonth = now.get(Calendar.MONTH);
                int currentYear = now.get(Calendar.YEAR);
                int lastClaimedMonth = getConfig().getInt("rewards." + playerName + ".month", -1);
                int lastClaimedYear = getConfig().getInt("rewards." + playerName + ".year", -1);

                if (currentYear != lastClaimedYear || currentMonth != lastClaimedMonth) {
                    String rewardCommand = getConfig().getString("reward-command").replace("%player%", playerName);
                    getServer().dispatchCommand(getServer().getConsoleSender(), rewardCommand);
                    event.getPlayer().sendMessage("You have claimed your monthly reward.");

                    getConfig().set("rewards." + playerName + ".month", currentMonth);
                    getConfig().set("rewards." + playerName + ".year", currentYear);
                    saveConfig();
                }
            }
        }.runTaskLater(this, 1200L);
    }
}