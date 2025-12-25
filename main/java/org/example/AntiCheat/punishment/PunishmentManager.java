package org.example.AntiCheat.punishment;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.example.AntiCheat.data.PlayerData;
import java.util.UUID;
import java.util.logging.Logger;

public class PunishmentManager {

    private final Logger logger;
    private final boolean stagedPunishments;
    private final int warnThreshold;
    private final int kickThreshold;

    public PunishmentManager(Logger logger, boolean stagedPunishments, int warnThreshold, int kickThreshold) {
        this.logger = logger;
        this.stagedPunishments = stagedPunishments;
        this.warnThreshold = warnThreshold;
        this.kickThreshold = kickThreshold;
    }

    public void handleViolation(Player player, ViolationType type, double violationLevel, PlayerData data) {
        if (stagedPunishments) {
            if (violationLevel >= kickThreshold) {
                kickPlayer(player, "Repeated violations: " + type.getDisplayName());
                resetViolation(data, type);
            } else if (violationLevel >= warnThreshold) {
                player.sendMessage(ChatColor.RED + "[AntiCheat] " + ChatColor.YELLOW +
                        "Warning: Suspicious activity detected");
                flagPlayer(player, type.getDisplayName(), String.format("VL: %.1f", violationLevel));
            } else {
                flagPlayer(player, type.getDisplayName(), String.format("VL: %.1f", violationLevel));
            }
        }
    }

    public void kickPlayer(Player player, String reason) {
        String kickMessage = ChatColor.RED + "Kicked from the server: " + reason;
        String alertMessage = ChatColor.RED + "[AntiCheat] " + ChatColor.YELLOW + player.getName()
                + ChatColor.WHITE + " Kicked: " + reason;

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.isOp()) {
                staff.sendMessage(alertMessage);
            }
        }

        logger.warning(player.getName() + " kicked for " + reason);
        player.kickPlayer(kickMessage);
    }

    public void flagPlayer(Player player, String checkType, String details) {
        String message = ChatColor.RED + "[AntiCheat] " + ChatColor.YELLOW + player.getName()
                + ChatColor.WHITE + " flagged for " + ChatColor.RED + checkType
                + ChatColor.WHITE + " (" + details + ")";

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("anticheat.notify")) {
                staff.sendMessage(message);
            }
        }

        logger.warning(player.getName() + " flagged for " + checkType + " - " + details);
    }

    private void resetViolation(PlayerData data, ViolationType type) {
        switch (type) {
            case SPEED:
                data.speedViolationLevel = 0.0;
                break;
            case FLY:
                data.flyViolationLevel = 0.0;
                data.yMotionHistory.clear();
                data.flyBuffer = 0;
                break;
            case CPS:
                data.cpsViolationLevel = 0.0;
                data.cpsBuffer = 0.0;
                break;
            case REACH:
                data.reachViolationLevel = 0.0;
                data.reachBuffer = 0.0;
                break;
            case FASTBREAK:
                data.fastBreakViolationLevel = 0.0;
                break;
        }
    }
}