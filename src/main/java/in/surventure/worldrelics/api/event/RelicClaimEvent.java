package in.surventure.worldrelics.api.event;

import in.surventure.worldrelics.model.ActiveRelic;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RelicClaimEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final ActiveRelic relic;
    private final Player player;
    private boolean cancelled = false;

    public RelicClaimEvent(ActiveRelic relic, Player player) {
        this.relic = relic;
        this.player = player;
    }

    public ActiveRelic getRelic() {
        return relic;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
