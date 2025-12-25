package org.example.AntiCheat.checks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.example.AntiCheat.data.PlayerData;
import org.example.AntiCheat.punishment.PunishmentManager;
import org.example.AntiCheat.punishment.ViolationType;
import org.example.AntiCheat.util.MathUtil;

import java.util.UUID;

public class ReachCheck implements Check {

    private final PunishmentManager punishmentManager;
    private final double reachTolerance;
    private final int reachSamples;
    private final boolean enabled;

    public ReachCheck(PunishmentManager punishmentManager, double reachTolerance, int reachSamples, boolean enabled) {
        this.punishmentManager = punishmentManager;
        this.reachTolerance = reachTolerance;
        this.reachSamples = reachSamples;
        this.enabled = enabled;
    }

    @Override
    public void check(Player attacker, PlayerData data, UUID uuid) {
        // This check is called from EntityDamageByEntityEvent with victim parameter
        // For now, we'll keep it empty and handle it in the event listener
    }

    public void checkReach(Player attacker, Player victim, PlayerData data, UUID uuid) {
        Location attackerEye = attacker.getEyeLocation();
        Location victimEye = victim.getEyeLocation();

        double distance = attackerEye.distance(victimEye);
        double reachLimit = reachTolerance + 0.3;

        if (distance > reachLimit) {
            data.reachBuffer += 1.0;
        } else {
            data.reachBuffer = Math.max(0, data.reachBuffer - 1.0);
        }

        data.reachHistory.add(distance);

        if (data.reachHistory.size() > reachSamples) {
            data.reachHistory.poll();
        }

        if (data.reachBuffer >= 3.0 && data.reachHistory.size() >= reachSamples) {
            double avgReach = MathUtil.calculateAverage(data.reachHistory);

            punishmentManager.flagPlayer(attacker, "Reach", String.format("%.2f blocks", avgReach));
            data.reachViolationLevel += 0.5;
            punishmentManager.handleViolation(attacker, ViolationType.REACH, data.reachViolationLevel, data);

            data.reachBuffer = 0;
        }
    }

    @Override
    public String getCheckName() {
        return "ReachCheck";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}