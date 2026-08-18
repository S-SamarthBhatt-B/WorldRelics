package in.surventure.worldrelics.api.event;

import in.surventure.worldrelics.model.ActiveRelic;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RelicExpireEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final ActiveRelic relic;

    public RelicExpireEvent(ActiveRelic relic) {
        this.relic = relic;
    }

    public ActiveRelic getRelic() {
        return relic;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
