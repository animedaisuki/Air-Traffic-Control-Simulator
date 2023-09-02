package towersim.util;

/**
 * Denotes a class whose state changes on every tick of the simulation.
 */
public interface Tickable {
    /**
     * Method to be called once on every simulation tick.
     */
    void tick();
}
