package in.surventure.worldrelics.api.event;

import in.surventure.worldrelics.model.ActiveRelic;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RelicDropEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final ActiveRelic relic;
    private final Player formerOwner;
    private final Location dropLocation;

    public RelicDropEvent(ActiveRelic relic, Player formerOwner, Location dropLocation) {
        this.relic = relic;
        this.formerOwner = formerOwner;
        this.dropLocation = dropLocation;
    }

    public ActiveRelic getRelic() {
        return relic;
    }

    public Player getFormerOwner() {
        return formerOwner;
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
