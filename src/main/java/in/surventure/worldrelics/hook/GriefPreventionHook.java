package in.surventure.worldrelics.hook;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GriefPreventionHook implements ProtectionHook {

    @Override
    public String getName() {
        return "GriefPrevention";
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");
    }

    @Override
    public boolean isClaimedOrProtected(Location location) {
        if (!isAvailable() || location == null) return false;
        try {
            Class<?> gpClass = Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
            Object instance = gpClass.getField("instance").get(null);
            Object dataStore = gpClass.getMethod("getDataStore").invoke(instance);
            Object claim = dataStore.getClass().getMethod("getClaimAt", Location.class, boolean.class, Class.forName("me.ryanhamshire.GriefPrevention.Claim")).invoke(dataStore, location, true, null);
            return claim != null;
        } catch (Exception e) {
            return false;
        }
    }
}
