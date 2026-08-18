package in.surventure.worldrelics.model;

/**
 * State machine for the single server-wide World Relic instance.
 */
public enum RelicState {
    NO_RELIC,
    RESPAWNING,
    SEARCHING,
    AVAILABLE,
    CLAIMED,
    EXPIRED
}
