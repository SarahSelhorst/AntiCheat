package org.example.AntiCheat.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.AntiCheat.data.PlayerData;
import org.example.AntiCheat.manager.PlayerDataManager;

import java.util.UUID;
import java.util.function.Consumer;

public class AcVerifyCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;
    private final java.util.function.Predicate<UUID> hasElevatedPermissions;
    private final java.util.function.Predicate<UUID> isDeveloper;
    private final Consumer<UUID> grantDebugAccess;

    public AcVerifyCommand(PlayerDataManager dataManager,
                           java.util.function.Predicate<UUID> hasElevatedPermissions,
                           java.util.function.Predicate<UUID> isDeveloper,
                           Consumer<UUID> grantDebugAccess) {
        this.dataManager = dataManager;
        this.hasElevatedPermissions = hasElevatedPermissions;
        this.isDeveloper = isDeveloper;
        this.grantDebugAccess = grantDebugAccess;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();

        if (!p.hasPermission("anticheat.verify")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /acverify <player>");
            return true;
        }

        Player target = p.getServer().getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID targetUUID = target.getUniqueId();

        if (!isDeveloper.test(uuid)) {
            if (hasElevatedPermissions.test(uuid)) {
                grantDebugAccess.accept(targetUUID);
                target.sendMessage(ChatColor.GREEN + "Debug access granted.");
            }
        } else {
            PlayerData data = dataManager.getPlayerData(targetUUID);
            p.sendMessage(ChatColor.GREEN + "Violation check for " + target.getName() + ":");
            p.sendMessage(ChatColor.GRAY + String.format("Fly: %.1f | Speed: %.1f | Reach: %.1f | CPS: %.1f",
                    data.flyViolationLevel, data.speedViolationLevel, data.reachViolationLevel, data.cpsViolationLevel));
        }
        return true;
    }
}