package in.surventure.worldrelics.database;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.PlayerStats;
import in.surventure.worldrelics.model.RelicHistoryEntry;
import in.surventure.worldrelics.model.RelicRarity;
import in.surventure.worldrelics.model.RelicState;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final WorldRelicsPlugin plugin;
    private Connection connection;
    private final File dbFile;

    public DatabaseManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("database.file", "worldrelics.db"));
    }

    public void initialize() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
            plugin.getLogger().info("[WorldRelics] SQLite Database initialized successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("[WorldRelics] Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS active_relic (" +
                    "relic_uuid TEXT PRIMARY KEY, " +
                    "relic_type TEXT NOT NULL, " +
                    "rarity TEXT NOT NULL, " +
                    "owner_uuid TEXT, " +
                    "owner_name TEXT, " +
                    "spawn_world TEXT NOT NULL, " +
                    "spawn_x REAL NOT NULL, " +
                    "spawn_y REAL NOT NULL, " +
                    "spawn_z REAL NOT NULL, " +
                    "claimed_at INTEGER NOT NULL, " +
                    "expires_at INTEGER NOT NULL, " +
                    "status TEXT NOT NULL" +
                    ");");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS relic_history (" +
                    "relic_uuid TEXT PRIMARY KEY, " +
                    "relic_type TEXT NOT NULL, " +
                    "owner_uuid TEXT, " +
                    "owner_name TEXT, " +
                    "claimed_at INTEGER NOT NULL, " +
                    "expired_at INTEGER NOT NULL, " +
                    "final_status TEXT NOT NULL" +
                    ");");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_stats (" +
                    "player_uuid TEXT PRIMARY KEY, " +
                    "relics_claimed INTEGER DEFAULT 0, " +
                    "relics_lost INTEGER DEFAULT 0, " +
                    "relic_kills INTEGER DEFAULT 0" +
                    ");");
        }
    }

    public synchronized ActiveRelic loadActiveRelicSync() {
        String sql = "SELECT * FROM active_relic LIMIT 1";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                UUID relicUuid = UUID.fromString(rs.getString("relic_uuid"));
                String relicType = rs.getString("relic_type");
                RelicRarity rarity = RelicRarity.fromString(rs.getString("rarity"));
                String ownerUuidStr = rs.getString("owner_uuid");
                UUID ownerUuid = ownerUuidStr != null ? UUID.fromString(ownerUuidStr) : null;
                String ownerName = rs.getString("owner_name");
                String world = rs.getString("spawn_world");
                double x = rs.getDouble("spawn_x");
                double y = rs.getDouble("spawn_y");
                double z = rs.getDouble("spawn_z");
                long claimedAt = rs.getLong("claimed_at");
                long expiresAt = rs.getLong("expires_at");
                RelicState status = RelicState.valueOf(rs.getString("status"));

                return new ActiveRelic(relicUuid, relicType, rarity, ownerUuid, ownerName, world, x, y, z, claimedAt, expiresAt, status);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[WorldRelics] Failed to load active relic from DB: " + e.getMessage());
        }
        return null;
    }

    public void saveOrUpdateActiveRelic(ActiveRelic relic) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (this) {
                String sql = "INSERT INTO active_relic (relic_uuid, relic_type, rarity, owner_uuid, owner_name, spawn_world, spawn_x, spawn_y, spawn_z, claimed_at, expires_at, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT(relic_uuid) DO UPDATE SET " +
                        "owner_uuid=excluded.owner_uuid, owner_name=excluded.owner_name, claimed_at=excluded.claimed_at, status=excluded.status;";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, relic.getRelicUuid().toString());
                    pstmt.setString(2, relic.getRelicTypeId());
                    pstmt.setString(3, relic.getRarity().name());
                    pstmt.setString(4, relic.getOwnerUuid() != null ? relic.getOwnerUuid().toString() : null);
                    pstmt.setString(5, relic.getOwnerName());
                    pstmt.setString(6, relic.getWorldName());
                    pstmt.setDouble(7, relic.getX());
                    pstmt.setDouble(8, relic.getY());
                    pstmt.setDouble(9, relic.getZ());
                    pstmt.setLong(10, relic.getClaimedAt());
                    pstmt.setLong(11, relic.getExpiresAt());
                    pstmt.setString(12, relic.getStatus().name());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().severe("[WorldRelics] Failed to save/update active relic: " + e.getMessage());
                }
            }
        });
    }

    public void deleteActiveRelic(UUID relicUuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (this) {
                String sql = "DELETE FROM active_relic WHERE relic_uuid = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, relicUuid.toString());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().severe("[WorldRelics] Failed to delete active relic: " + e.getMessage());
                }
            }
        });
    }

    public void archiveHistory(RelicHistoryEntry entry) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (this) {
                String sql = "INSERT OR REPLACE INTO relic_history (relic_uuid, relic_type, owner_uuid, owner_name, claimed_at, expired_at, final_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, entry.getRelicUuid().toString());
                    pstmt.setString(2, entry.getRelicTypeId());
                    pstmt.setString(3, entry.getOwnerUuid() != null ? entry.getOwnerUuid().toString() : null);
                    pstmt.setString(4, entry.getOwnerName());
                    pstmt.setLong(5, entry.getClaimedAt());
                    pstmt.setLong(6, entry.getExpiredAt());
                    pstmt.setString(7, entry.getFinalStatus());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().severe("[WorldRelics] Failed to archive relic history: " + e.getMessage());
                }
            }
        });
    }

    public CompletableFuture<List<RelicHistoryEntry>> getHistoryAsync() {
        return CompletableFuture.supplyAsync(() -> {
            List<RelicHistoryEntry> list = new ArrayList<>();
            synchronized (this) {
                String sql = "SELECT * FROM relic_history ORDER BY expired_at DESC LIMIT 20";
                try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        UUID relicUuid = UUID.fromString(rs.getString("relic_uuid"));
                        String relicType = rs.getString("relic_type");
                        String ownerUuidStr = rs.getString("owner_uuid");
                        UUID ownerUuid = ownerUuidStr != null ? UUID.fromString(ownerUuidStr) : null;
                        String ownerName = rs.getString("owner_name");
                        long claimedAt = rs.getLong("claimed_at");
                        long expiredAt = rs.getLong("expired_at");
                        String finalStatus = rs.getString("final_status");

                        list.add(new RelicHistoryEntry(relicUuid, relicType, ownerUuid, ownerName, claimedAt, expiredAt, finalStatus));
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("[WorldRelics] Failed to fetch history: " + e.getMessage());
                }
            }
            return list;
        });
    }

    public synchronized List<PlayerStats> getTopPlayers(int limit) {
        List<PlayerStats> list = new ArrayList<>();
        String sql = "SELECT * FROM player_stats ORDER BY relics_claimed DESC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                    int claimed = rs.getInt("relics_claimed");
                    int lost = rs.getInt("relics_lost");
                    int kills = rs.getInt("relic_kills");
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    list.add(new PlayerStats(uuid, name != null ? name : "Unknown", claimed, lost, kills));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("[WorldRelics] Failed to fetch top players: " + e.getMessage());
        }
        return list;
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[WorldRelics] Error closing DB: " + e.getMessage());
        }
    }
}
