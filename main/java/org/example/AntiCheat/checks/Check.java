package org.example.AntiCheat.checks;

import org.example.AntiCheat.data.PlayerData;
import org.bukkit.entity.Player;
import java.util.UUID;

public interface Check {

    void check(Player player, PlayerData data, UUID uuid);

    String getCheckName();

    boolean isEnabled();
}