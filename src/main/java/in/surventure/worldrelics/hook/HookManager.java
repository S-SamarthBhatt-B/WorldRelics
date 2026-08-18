package in.surventure.worldrelics.hook;

import in.surventure.worldrelics.WorldRelicsPlugin;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class HookManager {

    private final WorldRelicsPlugin plugin;
    private final List<ProtectionHook> protectionHooks = new ArrayList<>();
    private final VaultHook vaultHook;

    public HookManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
        this.vaultHook = new VaultHook();

        registerHook(new WorldGuardHook());
        registerHook(new GriefPreventionHook());
        registerHook(new LandsHook());
    }

    public void registerHook(ProtectionHook hook) {
        protectionHooks.add(hook);
        if (hook.isAvailable()) {
            plugin.getLogger().info("[WorldRelics] Soft hook enabled: " + hook.getName());
        }
    }

    public boolean isLocationUnclaimed(Location location) {
        if (location == null) return true;
        for (ProtectionHook hook : protectionHooks) {
            if (hook.isAvailable() && hook.isClaimedOrProtected(location)) {
                return false;
            }
        }
        return true;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }
}
