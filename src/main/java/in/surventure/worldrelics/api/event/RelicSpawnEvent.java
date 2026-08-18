package in.surventure.worldrelics.api.event;

import in.surventure.worldrelics.model.ActiveRelic;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RelicSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final ActiveRelic relic;
    private final Location location;

    public RelicSpawnEvent(ActiveRelic relic, Location location) {
        this.relic = relic;
        this.location = location;
    }

    public ActiveRelic getRelic() {
        return relic;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
