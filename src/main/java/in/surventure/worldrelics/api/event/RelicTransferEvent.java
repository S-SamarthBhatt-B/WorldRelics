package in.surventure.worldrelics.api.event;

import in.surventure.worldrelics.model.ActiveRelic;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RelicTransferEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final ActiveRelic relic;
    private final Player previousOwner;
    private final Player newOwner;

    public RelicTransferEvent(ActiveRelic relic, Player previousOwner, Player newOwner) {
        this.relic = relic;
        this.previousOwner = previousOwner;
        this.newOwner = newOwner;
    }

    public ActiveRelic getRelic() {
        return relic;
    }

    public Player getPreviousOwner() {
        return previousOwner;
    }

    public Player getNewOwner() {
        return newOwner;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
