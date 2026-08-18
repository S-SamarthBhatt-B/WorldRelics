package in.surventure.worldrelics.manager;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.util.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RelicLocationManager {

    private final WorldRelicsPlugin plugin;
    private final Random random = new Random();

    public RelicLocationManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Location> findSafeLocationAsync() {
        CompletableFuture<Location> future = new CompletableFuture<>();

        List<String> allowedWorlds = plugin.getConfig().getStringList("spawn.allowed-worlds");
        if (allowedWorlds.isEmpty()) {
            allowedWorlds = List.of("world");
        }

        String worldName = allowedWorlds.get(random.nextInt(allowedWorlds.size()));
        World world = Bukkit.getWorld(worldName);
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        }

        if (world == null) {
            future.complete(null);
            return future;
        }

        final World targetWorld = world;
        double minDistance = plugin.getConfig().getDouble("spawn.min-distance", 3000.0);
        double maxDistance = plugin.getConfig().getDouble("spawn.max-distance", 10000.0);
        int maxAttempts = plugin.getConfig().getInt("spawn.max-attempts", 100);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location candidateLoc = null;
            int attempt = 0;

            while (attempt < maxAttempts) {
                attempt++;
                double angle = random.nextDouble() * 2 * Math.PI;
                double dist = minDistance + (random.nextDouble() * (maxDistance - minDistance));

                double x = dist * Math.cos(angle);
                double z = dist * Math.sin(angle);

                // Verify distance from ALL online players
                if (!LocationUtils.isFarFromAllPlayers(targetWorld, x, z, minDistance)) {
                    continue;
                }

                // Verify World Border
                WorldBorder border = targetWorld.getWorldBorder();
                Location center = border.getCenter();
                double size = border.getSize() / 2.0;
                if (Math.abs(x - center.getX()) >= size - 50 || Math.abs(z - center.getZ()) >= size - 50) {
                    continue;
                }

                // Check hooks (WorldGuard, GriefPrevention, Lands)
                if (!plugin.getHookManager().isLocationUnclaimed(new Location(targetWorld, x, 64, z))) {
                    continue;
                }

                // Perform safe main thread chunk load and surface height check
                candidateLoc = validateSurfaceSync(targetWorld, (int) x, (int) z);
                if (candidateLoc != null) {
                    break;
                }
            }

            // Fallback safe location if attempts exhausted
            if (candidateLoc == null) {
                candidateLoc = validateSurfaceSync(targetWorld, (int) minDistance, (int) minDistance);
            }

            future.complete(candidateLoc);
        });

        return future;
    }

    private Location validateSurfaceSync(World world, int x, int z) {
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                Block highest = world.getHighestBlockAt(x, z);
                if (highest.getType().isAir()) {
                    highest = highest.getRelative(BlockFace.DOWN);
                }

                if (highest.isLiquid() || highest.getType().name().contains("LAVA") || highest.getType().name().contains("WATER")) {
                    return null;
                }

                Block above1 = highest.getRelative(BlockFace.UP);
                Block above2 = above1.getRelative(BlockFace.UP);
                if (!above1.getType().isAir() && !above1.isPassable()) return null;
                if (!above2.getType().isAir() && !above2.isPassable()) return null;

                return highest.getLocation();
            }).get();
        } catch (Exception e) {
            return null;
        }
    }
}
