package in.surventure.worldrelics.manager;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicDefinition;
import in.surventure.worldrelics.model.RelicState;
import in.surventure.worldrelics.relic.ability.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RelicAbilityManager {

    private final WorldRelicsPlugin plugin;
    private final Map<String, RelicAbility> abilities = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public RelicAbilityManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
        registerDefaultAbilities();
    }

    private void registerDefaultAbilities() {
        registerAbility(new PhoenixRevivalAbility());
        registerAbility(new StormStrikeAbility());
        registerAbility(new FrostNovaAbility());
        registerAbility(new VoidStepAbility());
        registerAbility(new GuardianShieldAbility());
        registerAbility(new BloodSurgeAbility());
        registerAbility(new PhantomVeilAbility());
        registerAbility(new InfernoBurstAbility());

        registerAbility(new TimeWarpAbility());
        registerAbility(new ShadowBurstAbility());
        registerAbility(new SolarEruptionAbility());
        registerAbility(new TsunamiWaveAbility());
        registerAbility(new WindBurstAbility());
        registerAbility(new GravitySlamAbility());
    }

    public void registerAbility(RelicAbility ability) {
        abilities.put(ability.getName().toLowerCase(), ability);
    }

    public boolean triggerAbility(Player player, String abilityName, Map<String, Object> config) {
        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null || relic.getStatus() != RelicState.CLAIMED || !player.getUniqueId().equals(relic.getOwnerUuid())) {
            return false;
        }

        RelicAbility ability = abilities.get(abilityName.toLowerCase());
        if (ability == null) {
            return false;
        }

        long cooldownSec = ability.getCooldownSeconds();
        if (config != null && config.containsKey("cooldown")) {
            cooldownSec = ((Number) config.get("cooldown")).longValue();
        }

        long now = System.currentTimeMillis();
        Map<String, Long> playerCd = cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        long expireTime = playerCd.getOrDefault(abilityName.toLowerCase(), 0L);

        if (now < expireTime) {
            long remainingSec = (expireTime - now) / 1000 + 1;
            player.sendMessage(plugin.getMessageManager().getComponent("prefix").append(
                    net.kyori.adventure.text.Component.text("Ability " + ability.getName() + " is on cooldown for " + remainingSec + "s.", net.kyori.adventure.text.format.NamedTextColor.RED)
            ));
            return false;
        }

        boolean success = ability.trigger(plugin, player, relic, config);
        if (success) {
            playerCd.put(abilityName.toLowerCase(), now + (cooldownSec * 1000));
        }
        return success;
    }

    public void applyPassiveEffects(Player player, RelicDefinition def) {
        if (player == null || def == null) return;
        for (Map.Entry<PotionEffectType, Integer> entry : def.getPassiveEffects().entrySet()) {
            player.addPotionEffect(new PotionEffect(entry.getKey(), 40, entry.getValue(), false, false, true));
        }
    }
}
