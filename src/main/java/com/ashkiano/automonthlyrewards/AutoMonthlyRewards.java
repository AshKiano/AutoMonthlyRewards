package com.ashkiano.automonthlyrewards;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class AutoMonthlyRewards extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        Metrics metrics = new Metrics(this,21258);
        this.getLogger().info("Thank you for using the AutoMonthlyRewards plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");
        checkForUpdates();
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

    private void checkForUpdates() {
        try {
            String pluginName = this.getDescription().getName();
            URL url = new URL("https://www.ashkiano.com/version_check.php?plugin=" + pluginName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();
                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (jsonObject.has("error")) {
                    this.getLogger().warning("Error when checking for updates: " + jsonObject.getString("error"));
                } else {
                    String latestVersion = jsonObject.getString("latest_version");

                    String currentVersion = this.getDescription().getVersion();
                    if (currentVersion.equals(latestVersion)) {
                        this.getLogger().info("This plugin is up to date!");
                    } else {
                        this.getLogger().warning("There is a newer version (" + latestVersion + ") available! Please update!");
                    }
                }
            } else {
                this.getLogger().warning("Failed to check for updates. Response code: " + responseCode);
            }
        } catch (Exception e) {
            this.getLogger().warning("Failed to check for updates. Error: " + e.getMessage());
        }
    }
}