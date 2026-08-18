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

public class ShadowBurstAbility implements RelicAbility {

    @Override
    public String getName() {
        return "shadow-burst";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        player.getWorld().spawnParticle(Particle.SQUID_INK, player.getLocation().add(0, 1, 0), 80, 2.0, 1.5, 2.0, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.6f);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0));

        for (Entity e : player.getNearbyEntities(8.0, 8.0, 8.0)) {
            if (e instanceof LivingEntity target && !e.equals(player)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 120, 1));
            }
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#4B0082:#191970><bold>🌑 SHADOW BURST!</bold></gradient> Enemies blinded and withered."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 40;
    }
}
