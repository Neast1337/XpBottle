package org.neast.xpbottle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public final class XpBottle extends JavaPlugin implements CommandExecutor, Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private String prefix;
    private Logger logger;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.logger = getLogger(); // Initialise le logger
        this.saveDefaultConfig();
        loadConfig();
        getCommand("xpbottle").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);

        logger.info("XpBottle plugin enabled!"); // Log d'activation du plugin
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("XpBottle plugin disabled!"); // Log de désactivation du plugin
    }

    public void loadConfig() {
        logger.info("Loading configuration...");
        reloadConfig();
        FileConfiguration config = getConfig();
        prefix = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", "&a[XPBottle]&r "));
        logger.info("Configuration loaded with prefix: " + prefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + getConfig().getString("messages.only_players"));
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration config = getConfig();

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("xpbottle.reload")) {
                loadConfig();
                player.sendMessage(prefix + ChatColor.GREEN + config.getString("messages.config_reloaded"));
            } else {
                player.sendMessage(prefix + ChatColor.RED + config.getString("messages.no_permission_reload"));
            }
            return true;
        }

        if (!player.hasPermission("xpbottle.use")) {
            player.sendMessage(prefix + ChatColor.RED + config.getString("messages.no_permission_use"));
            return true;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long cooldownTime = config.getLong("cooldowns.default") * 1000;
        if (player.hasPermission("xpbottle.vipplus")) {
            cooldownTime = config.getLong("cooldowns.vipplus") * 1000;
        } else if (player.hasPermission("xpbottle.vip")) {
            cooldownTime = config.getLong("cooldowns.vip") * 1000;
        }

        if (!player.hasPermission("xpbottle.nocooldown") && cooldowns.containsKey(playerId) && currentTime - cooldowns.get(playerId) < cooldownTime) {
            long timeLeft = (cooldowns.get(playerId) + cooldownTime - currentTime) / 1000;
            String formattedTime = formatTime(timeLeft);
            player.sendMessage(prefix + ChatColor.RED + config.getString("messages.cooldown_wait").replace("{time}", formattedTime));
            return true;
        }

        int totalExperience = getTotalExperience(player);
        int bottles = totalExperience / 7; // 7 XP per bottle

        if (bottles == 0) {
            player.sendMessage(prefix + ChatColor.RED + config.getString("messages.not_enough_xp"));
            return true;
        }

        int remainingXP = totalExperience;
        int bottlesGiven = 0;

        while (bottles > 0 && player.getInventory().firstEmpty() != -1) {
            int bottlesToGive = Math.min(bottles, player.getInventory().getMaxStackSize());
            ItemStack expBottles = new ItemStack(Material.EXPERIENCE_BOTTLE, bottlesToGive);

            player.getInventory().addItem(expBottles);
            bottles -= bottlesToGive;
            bottlesGiven += bottlesToGive;
            remainingXP -= bottlesToGive * 7;
        }

        setPlayerExperience(player, remainingXP);

        cooldowns.put(playerId, currentTime);
        player.sendMessage(prefix + ChatColor.GREEN + config.getString("messages.xp_converted")
                .replace("{totalXP}", Integer.toString(totalExperience))
                .replace("{bottles}", Integer.toString(bottlesGiven)));

        if (bottles > 0) {
            player.sendMessage(prefix + ChatColor.RED + config.getString("messages.inventory_full"));
        }

        return true;
    }

    private int getTotalExperience(Player player) {
        int level = player.getLevel();
        int exp = Math.round(player.getExp() * player.getExpToLevel());
        return getTotalExperience(level) + exp;
    }

    private int getTotalExperience(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    private void setPlayerExperience(Player player, int exp) {
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        while (exp > 0) {
            int expToNextLevel = player.getExpToLevel();
            if (exp >= expToNextLevel) {
                exp -= expToNextLevel;
                player.giveExp(expToNextLevel);
            } else {
                player.giveExp(exp);
                exp = 0;
            }
        }
    }

    private String formatTime(long seconds) {
        FileConfiguration config = getConfig();

        long years = seconds / 31_536_000;
        seconds %= 31_536_000;
        long months = seconds / 2_592_000;
        seconds %= 2_592_000;
        long weeks = seconds / 604_800;
        seconds %= 604_800;
        long days = seconds / 86_400;
        seconds %= 86_400;
        long hours = seconds / 3_600;
        seconds %= 3_600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder timeString = new StringBuilder();

        if (years > 0) {
            timeString.append(config.getString("messages.time_format.years").replace("{years}", Long.toString(years))).append(" ");
        }
        if (months > 0) {
            timeString.append(config.getString("messages.time_format.months").replace("{months}", Long.toString(months))).append(" ");
        }
        if (weeks > 0) {
            timeString.append(config.getString("messages.time_format.weeks").replace("{weeks}", Long.toString(weeks))).append(" ");
        }
        if (days > 0) {
            timeString.append(config.getString("messages.time_format.days").replace("{days}", Long.toString(days))).append(" ");
        }
        if (hours > 0) {
            timeString.append(config.getString("messages.time_format.hours").replace("{hours}", Long.toString(hours))).append(" ");
        }
        if (minutes > 0) {
            timeString.append(config.getString("messages.time_format.minutes").replace("{minutes}", Long.toString(minutes))).append(" ");
        }
        if (seconds > 0 || timeString.length() == 0) { // Include seconds if it's the only unit
            timeString.append(config.getString("messages.time_format.seconds").replace("{seconds}", Long.toString(seconds)));
        }

        return timeString.toString().trim();
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        // Handle player experience change if needed
    }
}
