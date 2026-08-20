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

public class PhantomVeilAbility implements RelicAbility {

    @Override
    public String getName() {
        return "phantom-veil";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        int tier = relic != null ? relic.getTier() : 1;

        int durationSec = switch (tier) {
            case 2 -> 15;
            case 3 -> 25;
            default -> 10;
        };

        int speedAmp = tier >= 3 ? 2 : (tier == 2 ? 1 : 0);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationSec * 20, 0, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationSec * 20, speedAmp));

        if (tier >= 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationSec * 20, 1));
        }

        player.getWorld().spawnParticle(Particle.SQUID_INK, player.getLocation().add(0, 1, 0), 40 * tier, 0.3, 0.8, 0.3, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.2f);

        String tierBadge = tier == 3 ? " [Tier III Mastered]" : (tier == 2 ? " [Tier II Evolved]" : "");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#708090:#D3D3D3><bold>👻 PHANTOM VEIL" + tierBadge + "!</bold></gradient> Vanished for " + durationSec + "s."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 90;
    }
}
