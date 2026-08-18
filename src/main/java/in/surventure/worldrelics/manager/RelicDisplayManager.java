package in.surventure.worldrelics.manager;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicDefinition;
import in.surventure.worldrelics.model.RelicState;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RelicDisplayManager {

    private final WorldRelicsPlugin plugin;
    private BossBar activeBossBar;
    private UUID previousOwnerUuid;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RelicDisplayManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateDisplays() {
        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null || relic.getStatus() == RelicState.NO_RELIC || relic.getStatus() == RelicState.EXPIRED) {
            removeBossBar();
            clearOwnerEffects();
            return;
        }

        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
        String name = def != null ? def.getDisplayName() : relic.getRelicTypeId();
        String formattedTime = formatRemainingTime(relic.getRemainingMillis());
        String ownerName = relic.getOwnerName() != null ? relic.getOwnerName() : "Unclaimed";

        // Tick Beacon Beam if relic is unclaimed
        if (relic.getStatus() == RelicState.AVAILABLE) {
            plugin.getRelicManager().getStructureManager().tickBeaconBeam();
        }

        // Actionbar & Owner Effects update
        if (relic.getOwnerUuid() != null) {
            Player owner = Bukkit.getPlayer(relic.getOwnerUuid());
            if (owner != null && owner.isOnline()) {
                if (plugin.getConfig().getBoolean("actionbar.enabled", true)) {
                    Component abComponent = miniMessage.deserialize(
                            "⚡ <name> | <time> remaining",
                            Placeholder.parsed("name", name),
                            Placeholder.unparsed("time", formattedTime)
                    );
                    owner.sendActionBar(abComponent);
                }

                // Apply Owner Glowing Effect & Particles
                applyOwnerEffects(owner, def);
            }
        } else {
            clearOwnerEffects();
        }

        // Bossbar update
        if (plugin.getConfig().getBoolean("bossbar.enabled", true)) {
            Component bossTitle = miniMessage.deserialize(
                    "⚡ <name> <gray>|</gray> Owner: <gold><owner></gold> <gray>|</gray> Time: <aqua><time></aqua>",
                    Placeholder.parsed("name", name),
                    Placeholder.unparsed("owner", ownerName),
                    Placeholder.unparsed("time", formattedTime)
            );

            if (activeBossBar == null) {
                activeBossBar = BossBar.bossBar(bossTitle, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
            } else {
                activeBossBar.name(bossTitle);
                long totalMillis = (relic.getExpiresAt() - relic.getClaimedAt());
                if (totalMillis > 0) {
                    float progress = (float) relic.getRemainingMillis() / totalMillis;
                    activeBossBar.progress(Math.max(0.0f, Math.min(1.0f, progress)));
                }
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showBossBar(activeBossBar);
            }
        } else {
            removeBossBar();
        }
    }

    private void applyOwnerEffects(Player owner, RelicDefinition def) {
        if (previousOwnerUuid != null && !previousOwnerUuid.equals(owner.getUniqueId())) {
            Player prev = Bukkit.getPlayer(previousOwnerUuid);
            if (prev != null && prev.isOnline()) {
                prev.setGlowing(false);
            }
        }
        previousOwnerUuid = owner.getUniqueId();

        if (plugin.getConfig().getBoolean("owner-effects.glowing", true)) {
            owner.setGlowing(true);
        }

        if (plugin.getConfig().getBoolean("owner-effects.particles", true)) {
            Particle particle = Particle.END_ROD;
            if (def != null) {
                switch (def.getId().toLowerCase()) {
                    case "phoenix_heart", "inferno_core" -> particle = Particle.FLAME;
                    case "thunder_core" -> particle = Particle.CRIT;
                    case "frost_crown" -> particle = Particle.SNOWFLAKE;
                    case "void_eye" -> particle = Particle.DRAGON_BREATH;
                    case "blood_relic" -> particle = Particle.REDSTONE;
                }
            }
            if (particle == Particle.REDSTONE) {
                owner.getWorld().spawnParticle(Particle.REDSTONE, owner.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.05, new Particle.DustOptions(org.bukkit.Color.RED, 1.2f));
            } else {
                owner.getWorld().spawnParticle(particle, owner.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.02);
            }
        }
    }

    public void clearOwnerEffects() {
        if (previousOwnerUuid != null) {
            Player prev = Bukkit.getPlayer(previousOwnerUuid);
            if (prev != null && prev.isOnline()) {
                prev.setGlowing(false);
            }
            previousOwnerUuid = null;
        }
    }

    public void removeBossBar() {
        if (activeBossBar != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.hideBossBar(activeBossBar);
            }
            activeBossBar = null;
        }
    }

    public String formatRemainingTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        hours = hours % 24;
        minutes = minutes % 60;

        if (days > 0) {
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return Math.max(1, minutes) + "m";
        }
    }
}
