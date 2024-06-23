package org.neast.bottlexp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class Bottlexp extends JavaPlugin implements Listener {

    private final HashMap<UUID, Long> lastUseTime = new HashMap<>();

    @Override
    public void onEnable() {
        // Save the default config
        this.saveDefaultConfig();

        // Register the command executors
        this.getCommand("xpbottle").setExecutor(new XPBottleCommand());
        this.getCommand("xpbottle reload").setExecutor(new ReloadXPConfigCommand());

        // Register the event listener
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private String formatMessage(String message) {
        String prefix = getConfig().getString("prefix", "&7[&bXPBottle&7] ");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public class XPBottleCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("bottlexp.reload")) {
                    reloadConfig();
                    sender.sendMessage(formatMessage(ChatColor.GREEN + getConfig().getString("messages.reload_success")));
                } else {
                    sender.sendMessage(formatMessage(ChatColor.RED + getConfig().getString("messages.no_permission_reload")));
                }
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID playerId = player.getUniqueId();

                // Check if the player's inventory has space
                if (!hasInventorySpace(player)) {
                    player.sendMessage(formatMessage(ChatColor.RED + getConfig().getString("messages.inventory_full", "Your inventory is full!")));
                    return true; // Stop further execution if inventory is full
                }

                if (!player.isOp() && lastUseTime.containsKey(playerId)) {
                    long lastUse = lastUseTime.get(playerId);
                    long currentTime = System.currentTimeMillis();
                    long cooldown = 900000; // 15 minutes in milliseconds

                    if (currentTime - lastUse < cooldown) {
                        long timeLeft = cooldown - (currentTime - lastUse);
                        player.sendMessage(formatMessage(ChatColor.RED + getConfig().getString("messages.cooldown_message").replace("{time}", formatTime(timeLeft))));
                        return true;
                    }
                }

                int levels = player.getLevel();
                if (levels > 0) {
                    // Remove XP levels only if the XP bottle can be given successfully
                    if (giveXPBottle(player, levels)) {
                        player.giveExpLevels(-levels);
                        player.sendMessage(formatMessage(ChatColor.GREEN + getConfig().getString("messages.convert_success")));

                        // Update the last use time
                        lastUseTime.put(playerId, System.currentTimeMillis());
                    } else {
                        player.sendMessage(formatMessage(ChatColor.RED + getConfig().getString("messages.inventory_full", "Votre inventaire est plein!")));
                    }
                } else {
                    player.sendMessage(formatMessage(ChatColor.RED + getConfig().getString("messages.not_enough_xp")));
                }
                return true;
            }

            sender.sendMessage(formatMessage(ChatColor.RED + getConfig().getString("messages.player_only")));
            return false;
        }

        private boolean hasInventorySpace(Player player) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType() == Material.AIR) {
                    return true;
                }
            }
            return false;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == Material.matchMaterial(getConfig().getString("item.type"))) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', getConfig().getString("item.display_name"))) &&
                    meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lore != null && !lore.isEmpty()) {
                    String loreLine = lore.get(0);
                    int levels = extractLevelsFromLore(loreLine);
                    if (levels > 0) {
                        player.giveExpLevels(levels);
                        String xpRecoveredMessage = getConfig().getString("messages.xp_recovered");
                        if (xpRecoveredMessage != null) {
                            player.sendMessage(formatMessage(ChatColor.GREEN + xpRecoveredMessage.replace("{levels}", String.valueOf(levels))));
                        } else {
                            player.sendMessage(formatMessage(ChatColor.GREEN + "Vous avez récupéré " + levels + " niveaux d'XP !"));
                        }
                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.getInventory().remove(item);
                        }
                    }
                }
            }
        }
    }

    private int extractLevelsFromLore(String loreLine) {
        String[] parts = loreLine.split(" ");
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public class ReloadXPConfigCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("bottlexp.reload")) {
                reloadConfig();
                sender.sendMessage(formatMessage(ChatColor.GREEN + getConfig().getString("messages.reload_success")));
            } else {
                sender.sendMessage(formatMessage(ChatColor.RED + getConfig().getString("messages.no_permission_reload")));
            }
            return true;
        }
    }

    private boolean giveXPBottle(Player player, int levels) {
        String itemTypeString = getConfig().getString("item.type", "PAPER");
        Material itemType = Material.matchMaterial(itemTypeString);
        if (itemType == null) {
            player.sendMessage(formatMessage(ChatColor.RED + "L'item configuré n'est pas valide. Utilisation de l'item par défaut (PAPER)."));
            itemType = Material.PAPER;
        }

        ItemStack xpItem = new ItemStack(itemType);
        ItemMeta meta = xpItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("item.display_name")));
            // Remove enchantment if it exists
            if (meta.hasEnchant(Enchantment.LUCK)) {
                meta.removeEnchant(Enchantment.LUCK);
            }
            meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7Contient " + levels + " niveaux d'XP")));

            // Set custom model data
            int customModelData = getConfig().getInt("item.custom_model_data", 0);
            meta.setCustomModelData(customModelData);

            xpItem.setItemMeta(meta);
        }

        // Check if the player's inventory has space
        HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(xpItem);
        if (!remainingItems.isEmpty()) {
            // Inventory is full, remove the partially added item and send an error message
            player.getInventory().removeItem(xpItem);
            return false;
        }
        return true;
    }

    private String formatTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long days = TimeUnit.MILLISECONDS.toDays(millis) % 7;
        long weeks = TimeUnit.MILLISECONDS.toDays(millis) / 7 % 4;
        long months = TimeUnit.MILLISECONDS.toDays(millis) / 30 % 12;
        long years = TimeUnit.MILLISECONDS.toDays(millis) / 365;

        StringBuilder timeString = new StringBuilder();
        if (years > 0) timeString.append(years).append(" ").append(getConfigString("time_units.years")).append(" ");
        if (months > 0) timeString.append(months).append(" ").append(getConfigString("time_units.months")).append(" ");
        if (weeks > 0) timeString.append(weeks).append(" ").append(getConfigString("time_units.weeks")).append(" ");
        if (days > 0) timeString.append(days).append(" ").append(getConfigString("time_units.days")).append(" ");
        if (hours > 0) timeString.append(hours).append(" ").append(getConfigString("time_units.hours")).append(" ");
        if (minutes > 0) timeString.append(minutes).append(" ").append(getConfigString("time_units.minutes")).append(" ");
        if (seconds > 0) timeString.append(seconds).append(" ").append(getConfigString("time_units.seconds"));

        return timeString.toString().trim();
    }

    private String getConfigString(String path) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path, path)); // Retourne le chemin s'il n'est pas trouvé
    }
}
