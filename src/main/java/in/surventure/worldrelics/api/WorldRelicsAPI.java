package in.surventure.worldrelics.api;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicState;
import org.bukkit.Location;

import java.util.UUID;

public class WorldRelicsAPI {

    private static WorldRelicsPlugin plugin;

    public static void setPlugin(WorldRelicsPlugin instance) {
        plugin = instance;
    }

    public static ActiveRelic getActiveRelic() {
        return plugin != null && plugin.getRelicManager() != null ? plugin.getRelicManager().getActiveRelic() : null;
    }

    public static boolean isRelicActive() {
        ActiveRelic relic = getActiveRelic();
        return relic != null && (relic.getStatus() == RelicState.AVAILABLE || relic.getStatus() == RelicState.CLAIMED);
    }

    public static UUID getRelicOwner() {
        ActiveRelic relic = getActiveRelic();
        return relic != null ? relic.getOwnerUuid() : null;
    }

    public static String getRelicType() {
        ActiveRelic relic = getActiveRelic();
        return relic != null ? relic.getRelicTypeId() : null;
    }

    public static Location getRelicLocation() {
        ActiveRelic relic = getActiveRelic();
        return relic != null ? relic.getLocation() : null;
    }

    public static long getRemainingTimeMillis() {
        ActiveRelic relic = getActiveRelic();
        return relic != null ? relic.getRemainingMillis() : 0L;
    }
}
