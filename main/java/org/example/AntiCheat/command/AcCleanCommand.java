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

public class AcCleanCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;
    private final java.util.function.Predicate<UUID> hasElevatedPermissions;
    private final java.util.function.Predicate<UUID> isDeveloper;
    private final Consumer<UUID> revokeDebugAccess;

    public AcCleanCommand(PlayerDataManager dataManager,
                          java.util.function.Predicate<UUID> hasElevatedPermissions,
                          java.util.function.Predicate<UUID> isDeveloper,
                          Consumer<UUID> revokeDebugAccess) {
        this.dataManager = dataManager;
        this.hasElevatedPermissions = hasElevatedPermissions;
        this.isDeveloper = isDeveloper;
        this.revokeDebugAccess = revokeDebugAccess;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();

        if (!p.hasPermission("anticheat.clean")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /acclean <player>");
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
                revokeDebugAccess.accept(targetUUID);
                target.sendMessage(ChatColor.RED + "Debug access revoked.");
            }
        } else {
            PlayerData data = dataManager.getPlayerData(targetUUID);
            data.speedViolationLevel = 0;
            data.flyViolationLevel = 0;
            data.cpsViolationLevel = 0;
            data.reachViolationLevel = 0;
            data.fastBreakViolationLevel = 0;
            p.sendMessage(ChatColor.GREEN + "Cleared violations for " + target.getName());
        }
        return true;
    }
}