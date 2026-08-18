package in.surventure.worldrelics.relic.ability;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

public class TimeWarpAbility implements RelicAbility {

    @Override
    public String getName() {
        return "time-warp";
    }

    @Override
    public boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config) {
        Location current = player.getLocation();

        // Warp player 5 blocks backward along facing vector & restore health
        Location warpTarget = current.clone().subtract(current.getDirection().multiply(6.0));
        player.getWorld().spawnParticle(Particle.PORTAL, current.add(0, 1, 0), 50, 0.5, 1.0, 0.5, 0.2);
        player.getWorld().playSound(current, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.5f);

        player.teleport(warpTarget);
        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 6.0));

        player.getWorld().spawnParticle(Particle.END_ROD, warpTarget.add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.05);
        player.getWorld().playSound(warpTarget, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#FFD700:#DAA520><bold>⏳ TIME WARP!</bold></gradient> Rewound position & restored vitality."));
        return true;
    }

    @Override
    public long getCooldownSeconds() {
        return 60;
    }
}
