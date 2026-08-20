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

public class BloodSurgeAbility implements RelicAbility {

    @Override
    public String getName() {
        return "blood-surge";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        int tier = relic != null ? relic.getTier() : 1;

        double healAmount = config.containsKey("heal-amount") ? ((Number) config.get("heal-amount")).doubleValue() : (4.0 + (tier * 4.0));
        int strengthAmp = tier - 1;

        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120 * tier, strengthAmp));

        if (tier >= 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 1));
        }

        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 40 * tier, 0.5, 1.0, 0.5, 0.1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 1.0f, 0.8f);

        String tierBadge = tier == 3 ? " [Tier III Mastered]" : (tier == 2 ? " [Tier II Evolved]" : "");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#8B0000:#FF0000><bold>🩸 BLOOD SURGE" + tierBadge + "!</bold></gradient> Restored vitality."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 40;
    }
}
