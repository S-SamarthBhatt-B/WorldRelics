package in.surventure.worldrelics.hook;

import org.bukkit.Location;

public interface ProtectionHook {

    String getName();

    boolean isAvailable();

    boolean isClaimedOrProtected(Location location);
}
