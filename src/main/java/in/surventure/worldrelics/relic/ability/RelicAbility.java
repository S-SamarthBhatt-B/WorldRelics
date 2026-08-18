package in.surventure.worldrelics.relic.ability;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import org.bukkit.entity.Player;

import java.util.Map;

public interface RelicAbility {

    String getName();

    boolean trigger(WorldRelicsPlugin plugin, Player player, ActiveRelic relic, Map<String, Object> config);

    long getCooldownSeconds();
}
