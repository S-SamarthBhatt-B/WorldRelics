package in.surventure.worldrelics.model;

import java.util.UUID;

public class RelicHistoryEntry {

    private final UUID relicUuid;
    private final String relicTypeId;
    private final UUID ownerUuid;
    private final String ownerName;
    private final long claimedAt;
    private final long expiredAt;
    private final String finalStatus;

    public RelicHistoryEntry(UUID relicUuid, String relicTypeId, UUID ownerUuid, String ownerName,
                             long claimedAt, long expiredAt, String finalStatus) {
        this.relicUuid = relicUuid;
        this.relicTypeId = relicTypeId;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.claimedAt = claimedAt;
        this.expiredAt = expiredAt;
        this.finalStatus = finalStatus;
    }

    public UUID getRelicUuid() {
        return relicUuid;
    }

    public String getRelicTypeId() {
        return relicTypeId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getClaimedAt() {
        return claimedAt;
    }

    public long getExpiredAt() {
        return expiredAt;
    }

    public String getFinalStatus() {
        return finalStatus;
    }
}
