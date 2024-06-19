package org.neast.bottlexp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Arrays;
import java.util.List;

public final class Bottlexp extends JavaPlugin implements Listener {

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

    public class XPBottleCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("bottlexp.reload")) {
                    reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + translateColors(getConfig().getString("messages.reload_success")));
                } else {
                    sender.sendMessage(ChatColor.RED + translateColors(getConfig().getString("messages.no_permission_reload")));
                }
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                int levels = player.getLevel();
                if (levels > 0) {
                    player.giveExpLevels(-levels);
                    giveXPBottle(player, levels);
                    player.sendMessage(ChatColor.GREEN + translateColors(getConfig().getString("messages.convert_success")));
                } else {
                    player.sendMessage(ChatColor.RED + translateColors(getConfig().getString("messages.not_enough_xp")));
                }
                return true;
            }
            sender.sendMessage(ChatColor.RED + translateColors(getConfig().getString("messages.player_only")));
            return false;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == Material.matchMaterial(getConfig().getString("item.type"))) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(translateColors(getConfig().getString("item.display_name"))) &&
                    meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lore != null && !lore.isEmpty()) {
                    String loreLine = lore.get(0);
                    int levels = extractLevelsFromLore(loreLine);
                    if (levels > 0) {
                        player.giveExpLevels(levels);
                        String xpRecoveredMessage = getConfig().getString("messages.xp_recovered");
                        if (xpRecoveredMessage != null) {
                            player.sendMessage(ChatColor.GREEN + translateColors(xpRecoveredMessage.replace("{levels}", String.valueOf(levels))));
                        } else {
                            player.sendMessage(ChatColor.GREEN + "Vous avez récupéré " + levels + " niveaux d'XP !");
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
                sender.sendMessage(ChatColor.GREEN + translateColors(getConfig().getString("messages.reload_success")));
            } else {
                sender.sendMessage(ChatColor.RED + translateColors(getConfig().getString("messages.no_permission_reload")));
            }
            return true;
        }
    }

    private void giveXPBottle(Player player, int levels) {
        String itemTypeString = getConfig().getString("item.type", "PAPER");
        Material itemType = Material.matchMaterial(itemTypeString);
        if (itemType == null) {
            player.sendMessage(ChatColor.RED + translateColors("L'item configuré n'est pas valide. Utilisation de l'item par défaut (PAPER)."));
            itemType = Material.PAPER;
        }

        ItemStack xpItem = new ItemStack(itemType);
        ItemMeta meta = xpItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(translateColors(getConfig().getString("item.display_name")));
            // Remove enchantment if it exists
            if (meta.hasEnchant(Enchantment.LUCK)) {
                meta.removeEnchant(Enchantment.LUCK);
            }
            meta.setLore(Arrays.asList(translateColors(ChatColor.GRAY + "Contient " + levels + " niveaux d'XP")));

            // Set custom model data
            int customModelData = getConfig().getInt("item.custom_model_data", 0);
            meta.setCustomModelData(customModelData);

            xpItem.setItemMeta(meta);
        }

        player.getInventory().addItem(xpItem);
    }

    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
