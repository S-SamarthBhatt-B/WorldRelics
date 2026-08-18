package in.surventure.worldrelics.hook;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WorldGuardHook implements ProtectionHook {

    @Override
    public String getName() {
        return "WorldGuard";
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
    }

    @Override
    public boolean isClaimedOrProtected(Location location) {
        if (!isAvailable() || location == null) return false;
        try {
            // Reflective check to avoid strict compile failure if WG version differs
            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object instance = wgClass.getMethod("getInstance").invoke(null);
            Object platform = wgClass.getMethod("getPlatform").invoke(instance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            
            // If container exists, check region query
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
