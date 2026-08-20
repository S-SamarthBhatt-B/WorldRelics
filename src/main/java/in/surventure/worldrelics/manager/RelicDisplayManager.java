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
import org.bukkit.Location;
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
                // Check & trigger evolution check
                plugin.getRelicManager().getEvolutionManager().checkEvolutionProgress(relic, owner);

                String tierBadge = switch (relic.getTier()) {
                    case 2 -> plugin.getConfig().getString("evolution.tier-2.badge", "<gold>[Tier II Evolved]</gold>");
                    case 3 -> plugin.getConfig().getString("evolution.tier-3.badge", "<light_purple><bold>[Tier III Mastered]</bold></light_purple>");
                    default -> plugin.getConfig().getString("evolution.tier-1-badge", "<gray>[Tier I]</gray>");
                };

                if (plugin.getConfig().getBoolean("actionbar.enabled", true)) {
                    String abFormat = plugin.getConfig().getString("actionbar.format", "⚡ <name> <tier> | <time> remaining");
                    Component abComponent = miniMessage.deserialize(
                            abFormat,
                            Placeholder.parsed("name", name),
                            Placeholder.parsed("tier", tierBadge),
                            Placeholder.unparsed("time", formattedTime)
                    );
                    owner.sendActionBar(abComponent);
                }

                // Apply Owner Glowing Effect & Footstep Particles
                applyOwnerEffects(owner, def);
            }
        } else {
            clearOwnerEffects();
        }

        // Bossbar update
        if (plugin.getConfig().getBoolean("bossbar.enabled", true)) {
            String bossFormat = plugin.getConfig().getString("bossbar.title-format", "⚡ <name> <gray>|</gray> Owner: <gold><owner></gold> <gray>|</gray> Time: <aqua><time></aqua>");
            Component bossTitle = miniMessage.deserialize(
                    bossFormat,
                    Placeholder.parsed("name", name),
                    Placeholder.unparsed("owner", ownerName),
                    Placeholder.unparsed("time", formattedTime)
            );

            BossBar.Color barColor = BossBar.Color.YELLOW;
            try {
                barColor = BossBar.Color.valueOf(plugin.getConfig().getString("bossbar.color", "YELLOW").toUpperCase());
            } catch (Exception ignored) {}

            BossBar.Overlay barOverlay = BossBar.Overlay.PROGRESS;
            try {
                barOverlay = BossBar.Overlay.valueOf(plugin.getConfig().getString("bossbar.overlay", "PROGRESS").toUpperCase());
            } catch (Exception ignored) {}

            if (activeBossBar == null) {
                activeBossBar = BossBar.bossBar(bossTitle, 1.0f, barColor, barOverlay);
            } else {
                activeBossBar.name(bossTitle);
                activeBossBar.color(barColor);
                activeBossBar.overlay(barOverlay);
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

        boolean isInvisible = owner.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);

        if (plugin.getConfig().getBoolean("owner-effects.glowing", true)) {
            owner.setGlowing(!isInvisible);
        }

        if (!isInvisible && plugin.getConfig().getBoolean("owner-effects.particles", true)) {
            Particle particle = Particle.END_ROD;
            if (def != null) {
                switch (def.getId().toLowerCase()) {
                    case "phoenix_heart", "inferno_core" -> particle = Particle.FLAME;
                    case "thunder_core" -> particle = Particle.CRIT;
                    case "frost_crown" -> particle = Particle.SNOWFLAKE;
                    case "void_eye" -> particle = Particle.DRAGON_BREATH;
                    case "phantom_mask" -> particle = Particle.PORTAL;
                    case "blood_relic" -> particle = Particle.REDSTONE;
                    case "guardian_heart" -> particle = Particle.HEART;
                }
            }

            // Head aura particles
            if (particle == Particle.REDSTONE) {
                owner.getWorld().spawnParticle(Particle.REDSTONE, owner.getLocation().add(0, 1.8, 0), 3, 0.2, 0.2, 0.2, 0.05, new Particle.DustOptions(org.bukkit.Color.RED, 1.2f));
            } else {
                owner.getWorld().spawnParticle(particle, owner.getLocation().add(0, 1.8, 0), 3, 0.2, 0.2, 0.2, 0.02);
            }

            // Custom Walking Footstep Trail particles at feet
            Location feetLoc = owner.getLocation();
            if (particle == Particle.REDSTONE) {
                owner.getWorld().spawnParticle(Particle.REDSTONE, feetLoc, 2, 0.1, 0.05, 0.1, 0.01, new Particle.DustOptions(org.bukkit.Color.RED, 0.8f));
            } else {
                owner.getWorld().spawnParticle(particle, feetLoc, 2, 0.1, 0.05, 0.1, 0.01);
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
