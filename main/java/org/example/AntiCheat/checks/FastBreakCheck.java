package org.example.AntiCheat.checks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.example.AntiCheat.data.PlayerData;
import org.example.AntiCheat.punishment.PunishmentManager;
import org.example.AntiCheat.punishment.ViolationType;

import java.util.UUID;

public class FastBreakCheck implements Check {

    private final PunishmentManager punishmentManager;
    private final boolean enabled;

    public FastBreakCheck(PunishmentManager punishmentManager, boolean enabled) {
        this.punishmentManager = punishmentManager;
        this.enabled = enabled;
    }

    @Override
    public void check(Player player, PlayerData data, UUID uuid) {
        // This is called from BlockBreakEvent
    }

    public void checkFastBreak(Player player, Material blockType, PlayerData data, UUID uuid) {
        long currentTime = System.currentTimeMillis();

        if (data.lastBlockBreakTime > 0) {
            long timeDiff = currentTime - data.lastBlockBreakTime;
            double expectedTime = calculateBreakTime(player, blockType);

            if (timeDiff < expectedTime * 0.4) {
                punishmentManager.flagPlayer(player, "FastBreak",
                        String.format("%dms (expected: %.0fms)", timeDiff, expectedTime));
                data.fastBreakViolationLevel += 1.0;
                punishmentManager.handleViolation(player, ViolationType.FASTBREAK,
                        data.fastBreakViolationLevel, data);
            }
        }

        data.lastBlockBreakTime = currentTime;
    }

    private double calculateBreakTime(Player player, Material blockType) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        double baseTime = 500;

        if (blockType.getHardness() > 0) {
            baseTime = blockType.getHardness() * 400;
        }

        if (tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.EFFICIENCY) > 0) {
            int level = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.EFFICIENCY);
            baseTime *= Math.pow(0.85, level);
        }

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.HASTE)) {
                baseTime *= Math.pow(0.8, effect.getAmplifier() + 1);
            }
            if (effect.getType().equals(PotionEffectType.MINING_FATIGUE)) {
                baseTime *= Math.pow(1.5, effect.getAmplifier() + 1);
            }
        }

        return Math.max(baseTime, 80);
    }

    @Override
    public String getCheckName() {
        return "FastBreakCheck";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}