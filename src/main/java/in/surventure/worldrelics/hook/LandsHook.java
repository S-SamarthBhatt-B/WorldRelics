package in.surventure.worldrelics.hook;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LandsHook implements ProtectionHook {

    @Override
    public String getName() {
        return "Lands";
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("Lands");
    }

    @Override
    public boolean isClaimedOrProtected(Location location) {
        if (!isAvailable() || location == null) return false;
        try {
            Class<?> landsIntegration = Class.forName("me.angeschossen.lands.api.LandsIntegration");
            Object api = landsIntegration.getConstructor(Object.class).newInstance(Bukkit.getPluginManager().getPlugin("WorldRelics"));
            Object land = landsIntegration.getMethod("getLand", Location.class).invoke(api, location);
            return land != null;
        } catch (Exception e) {
            return false;
        }
    }
}
