package org.example.AntiCheat.checks;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.example.AntiCheat.data.PlayerData;
import org.example.AntiCheat.punishment.PunishmentManager;
import org.example.AntiCheat.punishment.ViolationType;
import org.example.AntiCheat.util.MathUtil;

import java.util.UUID;

public class SpeedCheck implements Check {

    private final PunishmentManager punishmentManager;
    private final double maxBaseSpeed;
    private final double maxHorizontalSpeed;
    private final boolean enabled;

    public SpeedCheck(PunishmentManager punishmentManager, double maxBaseSpeed, double maxHorizontalSpeed, boolean enabled) {
        this.punishmentManager = punishmentManager;
        this.maxBaseSpeed = maxBaseSpeed;
        this.maxHorizontalSpeed = maxHorizontalSpeed;
        this.enabled = enabled;
    }

    @Override
    public void check(Player player, PlayerData data, UUID uuid) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Location current = player.getLocation();
        Location last = data.lastLocation;

        if (last == null) {
            data.lastLocation = current;
            return;
        }

        double dx = current.getX() - last.getX();
        double dz = current.getZ() - last.getZ();
        double deltaXZ = MathUtil.calculateHorizontalDistance(dx, dz);

        if (deltaXZ < 0.01) return;

        double maxSpeed = calculateMaxSpeed(player);

        if (deltaXZ > maxSpeed) {
            data.speedViolationLevel += (deltaXZ - maxSpeed) * 0.5;
            punishmentManager.handleViolation(player, ViolationType.SPEED, data.speedViolationLevel, data);
        } else {
            data.speedViolationLevel = Math.max(0, data.speedViolationLevel - 0.1);
        }

        data.lastLocation = current;
    }

    private double calculateMaxSpeed(Player player) {
        double speed = maxBaseSpeed;

        if (player.isSprinting()) speed *= 1.3;

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.SPEED)) {
                speed *= (1.0 + 0.2 * (effect.getAmplifier() + 1));
            }
            if (effect.getType().equals(PotionEffectType.JUMP_BOOST)) {
                speed *= (1.0 + 0.05 * (effect.getAmplifier() + 1));
            }
            if (effect.getType().equals(PotionEffectType.SLOWNESS)) {
                speed *= 0.85;
            }
        }

        Location loc = player.getLocation();
        Material blockBelow = loc.subtract(0, 0.1, 0).getBlock().getType();

        if (blockBelow == Material.ICE || blockBelow == Material.PACKED_ICE || blockBelow == Material.BLUE_ICE) {
            speed *= 1.6;
        }
        if (blockBelow == Material.SOUL_SAND || blockBelow == Material.HONEY_BLOCK) {
            speed *= 0.4;
        }
        if (blockBelow == Material.WATER || blockBelow == Material.LAVA) {
            speed *= 0.5;
        }

        if (player.isInWater()) speed *= 0.3;

        return Math.min(speed, maxHorizontalSpeed);
    }

    @Override
    public String getCheckName() {
        return "SpeedCheck";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}