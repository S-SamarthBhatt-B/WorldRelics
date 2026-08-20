package in.surventure.worldrelics.relic.ability;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class PhoenixRevivalAbility implements RelicAbility {

    @Override
    public String getName() {
        return "phoenix-revival";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        int tier = relic != null ? relic.getTier() : 1;

        double targetHealth = switch (tier) {
            case 2 -> 16.0; // 8 hearts
            case 3 -> player.getMaxHealth(); // Full health (10 hearts)
            default -> 10.0; // 5 hearts
        };

        int fireResDuration = switch (tier) {
            case 2 -> 600; // 30s
            case 3 -> 1200; // 60s
            default -> 300; // 15s
        };

        player.setHealth(Math.min(player.getMaxHealth(), targetHealth));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100 * tier, tier - 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, fireResDuration, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120 * tier, tier - 1));

        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 40 * tier, 0.5, 1.0, 0.5, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

        String tierBadge = tier == 3 ? " [Tier III Mastered]" : (tier == 2 ? " [Tier II Evolved]" : "");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#FF4500:#FFA500><bold>🔥 PHOENIX REVIVAL ACTIVATED" + tierBadge + "!</bold></gradient>"));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 120;
    }
}
