package in.surventure.worldrelics.manager;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicState;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RelicDuelManager {

    private final WorldRelicsPlugin plugin;
    private final Map<UUID, UUID> pendingDuelRequests = new HashMap<>(); // Challenger UUID -> Owner UUID

    public RelicDuelManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendDuelRequest(Player challenger, Player owner) {
        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null || relic.getStatus() != RelicState.CLAIMED || relic.getOwnerUuid() == null) {
            challenger.sendMessage(MiniMessage.miniMessage().deserialize("<red>There is currently no active relic owner to challenge!</red>"));
            return;
        }

        if (!relic.getOwnerUuid().equals(owner.getUniqueId())) {
            challenger.sendMessage(MiniMessage.miniMessage().deserialize("<red>" + owner.getName() + " is not the current relic owner!</red>"));
            return;
        }

        if (challenger.getUniqueId().equals(owner.getUniqueId())) {
            challenger.sendMessage(MiniMessage.miniMessage().deserialize("<red>You cannot challenge yourself to a duel!</red>"));
            return;
        }

        pendingDuelRequests.put(challenger.getUniqueId(), owner.getUniqueId());

        challenger.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gold>⚔️ Sent a Relic Duel challenge to owner <yellow><owner></yellow>!</gold>",
                Placeholder.unparsed("owner", owner.getName())
        ));

        owner.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gold>⚔️ <yellow><challenger></yellow> has challenged you to a 1v1 Relic Duel! Type <yellow>/wr duel accept</yellow> to accept.</gold>",
                Placeholder.unparsed("challenger", challenger.getName())
        ));
    }

    public void acceptDuelRequest(Player owner) {
        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null || relic.getStatus() != RelicState.CLAIMED || !owner.getUniqueId().equals(relic.getOwnerUuid())) {
            owner.sendMessage(MiniMessage.miniMessage().deserialize("<red>You are not the current relic owner!</red>"));
            return;
        }

        UUID challengerUuid = null;
        for (Map.Entry<UUID, UUID> entry : pendingDuelRequests.entrySet()) {
            if (entry.getValue().equals(owner.getUniqueId())) {
                challengerUuid = entry.getKey();
                break;
            }
        }

        if (challengerUuid == null) {
            owner.sendMessage(MiniMessage.miniMessage().deserialize("<red>You have no pending duel challenges.</red>"));
            return;
        }

        Player challenger = org.bukkit.Bukkit.getPlayer(challengerUuid);
        pendingDuelRequests.remove(challengerUuid);

        if (challenger == null || !challenger.isOnline()) {
            owner.sendMessage(MiniMessage.miniMessage().deserialize("<red>The challenger is no longer online.</red>"));
            return;
        }

        plugin.getMessageManager().broadcastRaw(MiniMessage.miniMessage().deserialize(
                "<gold>⚔️ <bold>RELIC DUEL ACCEPTED!</bold></gold>\n<yellow><owner></yellow> <gray>has accepted a 1v1 Relic Duel from</gray> <yellow><challenger></yellow><gray>! The winner claims the relic!</gray>",
                Placeholder.unparsed("owner", owner.getName()),
                Placeholder.unparsed("challenger", challenger.getName())
        ));
    }
}
