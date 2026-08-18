package in.surventure.worldrelics.relic.ability;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class FrostNovaAbility implements RelicAbility {

    @Override
    public String getName() {
        return "frost-nova";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        double radius = config.containsKey("radius") ? ((Number) config.get("radius")).doubleValue() : 7.0;
        int durationSec = config.containsKey("duration-seconds") ? ((Number) config.get("duration-seconds")).intValue() : 5;

        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 100, radius, 1.0, radius, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);

        int count = 0;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, durationSec * 20, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationSec * 20, 0));
                count++;
            }
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#E0FFFF:#00BFFF><bold>❄️ FROST NOVA!</bold></gradient> Frozen " + count + " targets."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 45;
    }
}
