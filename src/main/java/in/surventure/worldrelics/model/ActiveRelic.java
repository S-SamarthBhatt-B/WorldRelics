package in.surventure.worldrelics.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class ActiveRelic {

    private final UUID relicUuid;
    private final String relicTypeId;
    private final RelicRarity rarity;
    private UUID ownerUuid;
    private String ownerName;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private long claimedAt;
    private final long expiresAt;
    private RelicState status;
    private int tier = 1;
    private int relicKills = 0;

    public ActiveRelic(UUID relicUuid, String relicTypeId, RelicRarity rarity, UUID ownerUuid,
                       String ownerName, String worldName, double x, double y, double z,
                       long claimedAt, long expiresAt, RelicState status) {
        this.relicUuid = relicUuid;
        this.relicTypeId = relicTypeId;
        this.rarity = rarity;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.claimedAt = claimedAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.tier = 1;
        this.relicKills = 0;
    }

    public UUID getRelicUuid() {
        return relicUuid;
    }

    public String getRelicTypeId() {
        return relicTypeId;
    }

    public RelicRarity getRarity() {
        return rarity;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public long getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(long claimedAt) {
        this.claimedAt = claimedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public RelicState getStatus() {
        return status;
    }

    public void setStatus(RelicState status) {
        this.status = status;
    }

    public Location getLocation() {
        if (worldName == null || Bukkit.getWorld(worldName) == null) {
            return null;
        }
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    public long getRemainingMillis() {
        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = Math.max(1, Math.min(3, tier));
    }

    public int getRelicKills() {
        return relicKills;
    }

    public void setRelicKills(int relicKills) {
        this.relicKills = relicKills;
    }
}
