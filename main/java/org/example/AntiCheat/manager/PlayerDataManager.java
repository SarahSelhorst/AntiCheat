package org.example.AntiCheat.manager;

import org.example.AntiCheat.data.PlayerData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> playerData;

    public PlayerDataManager() {
        this.playerData = new HashMap<>();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerData());
    }

    public void removePlayerData(UUID uuid) {
        playerData.remove(uuid);
    }

    public void clearAll() {
        playerData.clear();
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return playerData;
    }
}