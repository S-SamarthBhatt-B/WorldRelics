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
        player.setHealth(Math.min(player.getMaxHealth(), 10.0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));

        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 50, 0.5, 1.0, 0.5, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#FF4500:#FFA500><bold>🔥 PHOENIX REVIVAL ACTIVATED!</bold></gradient>"));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 120;
    }
}
