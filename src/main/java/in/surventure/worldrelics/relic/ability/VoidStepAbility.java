package in.surventure.worldrelics.relic.ability;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;

public class VoidStepAbility implements RelicAbility {

    @Override
    public String getName() {
        return "void-step";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        int distance = config.containsKey("distance") ? ((Number) config.get("distance")).intValue() : 12;

        Location start = player.getLocation();
        Block targetBlock = player.getTargetBlockExact(distance);
        Location dest;

        if (targetBlock != null) {
            dest = targetBlock.getLocation().add(0.5, 1.0, 0.5);
            dest.setYaw(start.getYaw());
            dest.setPitch(start.getPitch());
        } else {
            dest = start.add(start.getDirection().multiply(distance));
        }

        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, start.add(0, 1, 0), 40, 0.3, 0.5, 0.3, 0.05);
        player.getWorld().playSound(start, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);

        player.teleport(dest);

        player.getWorld().spawnParticle(Particle.PORTAL, dest.add(0, 1, 0), 40, 0.3, 0.5, 0.3, 0.1);
        player.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#8A2BE2:#4B0082><bold>🔮 VOID STEP!</bold></gradient> Teleported through shadow."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 30;
    }
}
