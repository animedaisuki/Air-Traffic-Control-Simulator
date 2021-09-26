package towersim.util;

/**
 * Denotes a class whose state changes on every tick of the simulation.
 * @ass1
 */
public interface Tickable {
    /**
     * Method to be called once on every simulation tick.
     * @ass1
     */
    void tick();
}
