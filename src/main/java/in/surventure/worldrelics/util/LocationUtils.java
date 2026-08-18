package in.surventure.worldrelics.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public class LocationUtils {

    public static boolean isFarFromAllPlayers(World world, double candidateX, double candidateZ, double minDistance) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            return true;
        }

        double minDistanceSq = minDistance * minDistance;
        for (Player p : players) {
            if (p.getWorld().equals(world)) {
                Location pLoc = p.getLocation();
                double dx = pLoc.getX() - candidateX;
                double dz = pLoc.getZ() - candidateZ;
                double distSq = (dx * dx) + (dz * dz);
                if (distSq < minDistanceSq) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double getClosestPlayerDistance(Location relicLoc) {
        if (relicLoc == null || relicLoc.getWorld() == null) return -1;
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) return -1;

        double minDistance = Double.MAX_VALUE;
        for (Player p : players) {
            if (p.getWorld().equals(relicLoc.getWorld())) {
                double dist = p.getLocation().distance(relicLoc);
                if (dist < minDistance) {
                    minDistance = dist;
                }
            }
        }
        return minDistance == Double.MAX_VALUE ? -1 : minDistance;
    }

    public static String getFuzzyDirectionString(Location from, Location to) {
        if (from == null || to == null) return "unknown";
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) {
            angle += 360;
        }

        if (angle >= 337.5 || angle < 22.5) return "northern";
        if (angle >= 22.5 && angle < 67.5) return "north-eastern";
        if (angle >= 67.5 && angle < 112.5) return "eastern";
        if (angle >= 112.5 && angle < 157.5) return "south-eastern";
        if (angle >= 157.5 && angle < 202.5) return "southern";
        if (angle >= 202.5 && angle < 247.5) return "south-western";
        if (angle >= 247.5 && angle < 292.5) return "western";
        if (angle >= 292.5 && angle < 337.5) return "north-western";
        return "distant";
    }
}
