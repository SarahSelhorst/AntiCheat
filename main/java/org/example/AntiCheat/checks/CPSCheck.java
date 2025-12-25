package org.example.AntiCheat.checks;

import org.bukkit.entity.Player;
import org.example.AntiCheat.data.PlayerData;
import org.example.AntiCheat.punishment.PunishmentManager;
import org.example.AntiCheat.punishment.ViolationType;
import org.example.AntiCheat.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CPSCheck implements Check {

    private final PunishmentManager punishmentManager;
    private final int maxCPS;
    private final double cpsVarianceThreshold;
    private final boolean enabled;

    public CPSCheck(PunishmentManager punishmentManager, int maxCPS, double cpsVarianceThreshold, boolean enabled) {
        this.punishmentManager = punishmentManager;
        this.maxCPS = maxCPS;
        this.cpsVarianceThreshold = cpsVarianceThreshold;
        this.enabled = enabled;
    }

    @Override
    public void check(Player player, PlayerData data, UUID uuid) {
        long currentTime = System.nanoTime();
        data.clickTimestamps.add(currentTime);
        data.clickTimestamps.removeIf(time -> currentTime - time > 1_000_000_000L);

        int cps = data.clickTimestamps.size();

        if (cps > maxCPS) {
            data.cpsBuffer += 1.0;
        } else {
            data.cpsBuffer = Math.max(0, data.cpsBuffer - 0.25);
        }

        if (data.cpsBuffer >= 3.0) {
            punishmentManager.flagPlayer(player, "CPS", cps + " clicks/sec");
            data.cpsViolationLevel += 0.5;
            punishmentManager.handleViolation(player, ViolationType.CPS, data.cpsViolationLevel, data);
        }

        if (data.clickTimestamps.size() >= 10) {
            List<Long> timestamps = new ArrayList<>(data.clickTimestamps);
            List<Long> intervals = new ArrayList<>();

            for (int i = 1; i < timestamps.size(); i++) {
                intervals.add(timestamps.get(i) - timestamps.get(i - 1));
            }

            double avgInterval = intervals.stream().mapToLong(Long::longValue).average().orElse(0);
            double variance = 0;
            for (long interval : intervals) {
                variance += Math.pow(interval - avgInterval, 2);
            }
            variance /= intervals.size();

            double stdDev = MathUtil.calculateStandardDeviation(variance);
            double consistency = stdDev / Math.max(avgInterval, 1);

            if (consistency < cpsVarianceThreshold && cps > 14) {
                data.cpsViolationLevel += 0.5;
                punishmentManager.flagPlayer(player, "AutoClick Pattern", String.format("Consistency: %.3f", consistency));
            }
        }
    }

    @Override
    public String getCheckName() {
        return "CPSCheck";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}