package towersim.tasks;

/**
 * Enum to represent the possible types of tasks an aircraft can have.
 * <p>
 * Task types have a written description to explain what each task type means.
 * <table border="1">
 * <caption>Enum Definitions</caption>
 * <tr><th>TaskType</th>
 * <th>Written description
 * </th></tr>
 * <tr><td>{@code AWAY}</td><td>Flying outside the airport</td></tr>
 * <tr><td>{@code LAND}</td><td>Waiting in queue to land</td></tr>
 * <tr><td>{@code WAIT}</td><td>Waiting idle at gate</td></tr>
 * <tr><td>{@code LOAD}</td><td>Loading at gate</td></tr>
 * <tr><td>{@code TAKEOFF}</td><td>Waiting in queue to take off</td></tr>
 * </table>
 */
public enum TaskType {
    /**
     * {@code AWAY} means that aircraft are either flying or at other airports.
     */
    AWAY("Flying outside the airport"),

    /**
     * Aircraft in {@code LAND} are circling around the airport waiting for a slot to land.
     */
    LAND("Waiting in queue to land"),

    /**
     * {@code WAIT} tells an aircraft to stay stationary at a gate and not load any cargo.
     */
    WAIT("Waiting idle at gate"),

    /**
     * {@code LOAD} tasks represent the aircraft loading its cargo at the gate.
     * <p>
     * {@code LOAD} tasks also have a load percentage associated with them which tells the aircraft
     * how much cargo to load relative to their maximum capacity.
     */
    LOAD("Loading at gate"),

    /**
     * Aircraft in {@code TAKEOFF} are waiting on taxiways for a slot to take off.
     */
    TAKEOFF("Waiting in queue to take off");

    /** Short written description of the task type. */
    private final String description;

    TaskType(String description) {
        this.description = description;
    }

    /**
     * Returns the written description of this task type.
     * @return written description
     */
    public String getDescription() {
        return description;
    }
}
