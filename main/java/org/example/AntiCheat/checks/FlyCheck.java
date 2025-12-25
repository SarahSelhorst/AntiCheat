package org.example.AntiCheat.checks;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.example.AntiCheat.data.PlayerData;
import org.example.AntiCheat.punishment.PunishmentManager;
import org.example.AntiCheat.punishment.ViolationType;
import org.example.AntiCheat.util.MathUtil;

import java.util.UUID;

public class FlyCheck implements Check {

    private final PunishmentManager punishmentManager;
    private final int yMotionSamples;
    private final boolean enabled;

    public FlyCheck(PunishmentManager punishmentManager, int yMotionSamples, boolean enabled) {
        this.punishmentManager = punishmentManager;
        this.yMotionSamples = yMotionSamples;
        this.enabled = enabled;
    }

    @Override
    public void check(Player player, PlayerData data, UUID uuid) {
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        boolean isOnGround = player.isOnGround();

        if (isOnGround) {
            data.airTicks = 0;
            data.yMotionHistory.clear();
            data.flyBuffer = 0;
            return;
        }

        data.airTicks++;

        Material blockAt = player.getLocation().getBlock().getType();
        if (blockAt == Material.LADDER || blockAt == Material.VINE ||
                blockAt == Material.SCAFFOLDING || blockAt == Material.BUBBLE_COLUMN ||
                blockAt == Material.COBWEB) {
            return;
        }

        Location current = player.getLocation();
        Location last = data.lastLocation;

        if (last == null) return;

        double yMotion = current.getY() - last.getY();
        data.yMotionHistory.add(yMotion);

        if (data.yMotionHistory.size() > yMotionSamples) {
            data.yMotionHistory.poll();
        }

        if (data.yMotionHistory.size() >= yMotionSamples && data.airTicks > 15) {
            double avgMotion = MathUtil.calculateAverage(data.yMotionHistory);
            double variance = MathUtil.calculateVariance(data.yMotionHistory, avgMotion);

            double expected = data.lastYMotion - 0.08;
            boolean consistentFall = Math.abs(yMotion - expected) < 0.01;

            if ((variance < 0.001 && Math.abs(avgMotion) > 0.05) || !consistentFall) {
                data.flyBuffer++;

                if (data.flyBuffer > 6) {
                    data.flyViolationLevel += 1.0;
                    punishmentManager.handleViolation(player, ViolationType.FLY, data.flyViolationLevel, data);
                }
            } else {
                data.flyBuffer = Math.max(0, data.flyBuffer - 1);
            }
        }

        data.lastYMotion = yMotion;
    }

    @Override
    public String getCheckName() {
        return "FlyCheck";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}