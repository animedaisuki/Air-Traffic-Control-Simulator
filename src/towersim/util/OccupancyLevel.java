package towersim.util;

/**
 * Denotes a class that has an inherent capacity and a current level of loading relative to that
 * capacity.
 * <p>
 * This loading level is called the occupancy level and ranges from 0 (completely empty) to 100
 * (completely at capacity).
 * @ass1
 */
public interface OccupancyLevel {
    /**
     * Returns the current occupancy level of this entity as a percentage from 0 to 100.
     * 
     * @return occupancy level, 0 to 100
     * @ass1
     */
    int calculateOccupancyLevel();
}
