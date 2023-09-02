package towersim.tasks;

import java.util.Objects;

/**
 * Represents a task currently assigned to an aircraft.
 * <p>
 * Tasks relate to an aircraft's movement and ground operations.
 */
public class Task {
    /**
     * Type of task.
     */
    private final TaskType type;

    /**
     * Percent of maximum capacity to be loaded at the gate. Used by LOAD type tasks.
     */
    private final int loadPercent;

    /**
     * Creates a new Task of the given task type.
     *
     * @param type type of task
     */
    public Task(TaskType type) {
        this.type = type;
        this.loadPercent = 0;
    }

    /**
     * Creates a new Task of the given task type and stores the given load percentage in the task.
     * <p>
     * This constructor is used for tasks of the LOAD type, so that a percentage may be specified
     * for the load operation.
     *
     * @param type        type of task
     * @param loadPercent percentage of maximum capacity to load
     */
    public Task(TaskType type, int loadPercent) {
        this.type = type;
        this.loadPercent = loadPercent;
    }

    /**
     * Returns the type of this task.
     *
     * @return task type
     */
    public TaskType getType() {
        return type;
    }

    /**
     * Returns the load percentage specified when constructing the task, or 0 if none was specified.
     *
     * @return task load percentage
     */
    public int getLoadPercent() {
        return loadPercent;
    }

    /**
     * Returns the human-readable string representation of this task.
     * <p>
     * The format of the string to return is:
     * <ul>
     * <li>{@code "LOAD at percent%"} where {@code percent} is the load percentage, without the
     * enclosing double quotes, if the task type is {@code LOAD}.</li>
     * <li>{@code "taskType"} where {@code taskType} is the string representation of the task type,
     * without the enclosing double quotes, for any other task type.</li>
     * </ul>
     *
     * @return string representation of this task
     */
    @Override
    public String toString() {
        if (this.type == TaskType.LOAD) {
            return this.type + " at " + this.loadPercent + "%";
        }
        return String.valueOf(this.type);
    }

    /**
     * Returns the machine-readable string representation of this task.
     *
     * @return encoded string representation of this task
     */
    public String encode() {
        String task = "";
        if (this.type == TaskType.LOAD) {
            task += "LOAD@" + this.getLoadPercent();
        } else {
            task += this.getType();
        }
        return task;
    }

    /**
     * Returns true if and only if this task is equal to the other given task.
     *
     * @param obj other object to check equality
     * @return true if equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Task)) {
            return false;
        }
        Task task = (Task) obj;
        if (this.type == task.type && this.loadPercent == task.loadPercent) {
            return true;
        }
        return false;
    }

    /**
     * Returns the hash code of this task.
     *
     * @return hash code of this task
     */
    public int hashCode() {
        return Objects.hash(type, loadPercent);
    }
}
