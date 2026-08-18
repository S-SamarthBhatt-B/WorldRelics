package in.surventure.worldrelics.model;

import java.util.UUID;

public class PlayerStats {

    private final UUID playerUuid;
    private String playerName;
    private int relicsClaimed;
    private int relicsLost;
    private int relicKills;

    public PlayerStats(UUID playerUuid, int relicsClaimed, int relicsLost, int relicKills) {
        this(playerUuid, null, relicsClaimed, relicsLost, relicKills);
    }

    public PlayerStats(UUID playerUuid, String playerName, int relicsClaimed, int relicsLost, int relicKills) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.relicsClaimed = relicsClaimed;
        this.relicsLost = relicsLost;
        this.relicKills = relicKills;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        if (playerName != null) return playerName;
        org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(playerUuid);
        return op.getName() != null ? op.getName() : "Unknown";
    }

    public int getRelicsClaimed() {
        return relicsClaimed;
    }

    public void incrementRelicsClaimed() {
        this.relicsClaimed++;
    }

    public int getRelicsLost() {
        return relicsLost;
    }

    public void incrementRelicsLost() {
        this.relicsLost++;
    }

    public int getRelicKills() {
        return relicKills;
    }

    public void incrementRelicKills() {
        this.relicKills++;
    }
}
