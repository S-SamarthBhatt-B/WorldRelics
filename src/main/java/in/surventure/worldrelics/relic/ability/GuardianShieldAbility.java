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

public class GuardianShieldAbility implements RelicAbility {

    @Override
    public String getName() {
        return "guardian-shield";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        int durationSec = config.containsKey("duration-seconds") ? ((Number) config.get("duration-seconds")).intValue() : 8;

        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, durationSec * 20, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, durationSec * 20, 2));

        player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 1, 0), 50, 0.5, 1.0, 0.5, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 0.7f);

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#2E8B57:#00FF7F><bold>🛡️ GUARDIAN SHIELD ACTIVATED!</bold></gradient> (" + durationSec + "s)"));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 60;
    }
}
