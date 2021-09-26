package towersim.tasks;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a circular list of tasks for an aircraft to cycle through.
 *
 * @ass1
 */
public class TaskList {
    /**
     * List of tasks to cycle through.
     */
    private final List<Task> tasks;
    /**
     * Index of current task in tasks list.
     */
    private int currentTaskIndex;

    /**
     * Creates a new TaskList with the given list of tasks.
     * <p>
     * Initially, the current task (as returned by {@link #getCurrentTask()}) should be the first
     * task in the given list.
     *
     * @param tasks list of tasks
     * @ass1
     */
    public TaskList(List<Task> tasks) {
        this.tasks = tasks;
        this.currentTaskIndex = 0;
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException();
        }
        for (Task task : tasks) {
            if (task.getType() == TaskType.AWAY) {
                int pos = tasks.indexOf(task);
                if (pos < tasks.size() - 1) {
                    if (tasks.get(pos + 1).getType() != TaskType.AWAY
                            && tasks.get(pos + 1).getType() != TaskType.LAND) {
                        throw new IllegalArgumentException();
                    }
                }
                if (pos == tasks.size() - 1) {
                    if (tasks.get(0).getType() != TaskType.AWAY
                            && tasks.get(0).getType() != TaskType.LAND) {
                        throw new IllegalArgumentException();
                    }
                }
            }
            if (task.getType() == TaskType.LAND) {
                int pos = tasks.indexOf(task);
                if (pos < tasks.size() - 1) {
                    if (tasks.get(pos + 1).getType() != TaskType.WAIT
                            && tasks.get(pos + 1).getType() != TaskType.LOAD) {
                        throw new IllegalArgumentException();
                    }
                }
                if (pos == tasks.size() - 1) {
                    if (tasks.get(0).getType() != TaskType.WAIT
                            && tasks.get(0).getType() != TaskType.LOAD) {
                        throw new IllegalArgumentException();
                    }
                }
            }
            if (task.getType() == TaskType.WAIT) {
                int pos = tasks.indexOf(task);
                if (pos < tasks.size() - 1) {
                    if (tasks.get(pos + 1).getType() != TaskType.WAIT
                            && tasks.get(pos + 1).getType() != TaskType.LOAD) {
                        throw new IllegalArgumentException();
                    }
                }
                if (pos == tasks.size() - 1) {
                    if (tasks.get(0).getType() != TaskType.WAIT
                            && tasks.get(0).getType() != TaskType.LOAD) {
                        throw new IllegalArgumentException();
                    }
                }
            }
            if (task.getType() == TaskType.LOAD) {
                int pos = tasks.indexOf(task);
                if (pos < tasks.size() - 1) {
                    if (tasks.get(pos + 1).getType() != TaskType.TAKEOFF) {
                        throw new IllegalArgumentException();
                    }
                }
                if (pos == tasks.size() - 1) {
                    if (tasks.get(0).getType() != TaskType.TAKEOFF) {
                        throw new IllegalArgumentException();
                    }
                }
            }
            if (task.getType() == TaskType.TAKEOFF) {
                int pos = tasks.indexOf(task);
                if (pos < tasks.size() - 1) {
                    if (tasks.get(pos + 1).getType() != TaskType.AWAY) {
                        throw new IllegalArgumentException();
                    }
                }
                if (pos == tasks.size() - 1) {
                    if (tasks.get(0).getType() != TaskType.AWAY) {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
    }

    /**
     * Returns the current task in the list.
     *
     * @return current task
     * @ass1
     */
    public Task getCurrentTask() {
        return this.tasks.get(this.currentTaskIndex);
    }

    /**
     * Returns the task in the list that comes after the current task.
     * <p>
     * After calling this method, the current task should still be the same as it was before calling
     * the method.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * this method should return the first element of the list.
     *
     * @return next task
     * @ass1
     */
    public Task getNextTask() {
        int nextTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
        return this.tasks.get(nextTaskIndex);
    }

    /**
     * Moves the reference to the current task forward by one in the circular task list.
     * <p>
     * After calling this method, the current task should be the next task in the circular list
     * after the "old" current task.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * the new current task should be the first element of the list.
     *
     * @ass1
     */
    public void moveToNextTask() {
        this.currentTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
    }

    /**
     * Returns the human-readable string representation of this task list.
     * <p>
     * The format of the string to return is
     * <pre>TaskList currently on currentTask [taskNum/totalNumTasks]</pre>
     * where {@code currentTask} is the {@code toString()} representation of the current task as
     * returned by {@link Task#toString()},
     * {@code taskNum} is the place the current task occurs in the task list, and
     * {@code totalNumTasks} is the number of tasks in the task list.
     * <p>
     * For example, a task list with the list of tasks {@code [AWAY, LAND, WAIT, LOAD, TAKEOFF]}
     * which is currently on the {@code WAIT} task would have a string representation of
     * {@code "TaskList currently on WAIT [3/5]"}.
     *
     * @return string representation of this task list
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("TaskList currently on %s [%d/%d]",
                this.getCurrentTask(),
                this.currentTaskIndex + 1,
                this.tasks.size());
    }

    /**
     * Returns the machine-readable string representation of this task list.
     *
     * @return encoded string representation of this task list
     */
    public String encode() {
        String allTasks = "";
        if (this.getCurrentTask().getType() != TaskType.LOAD) {
            allTasks += this.getCurrentTask();
        } else {
            allTasks += "LOAD@" + this.getCurrentTask().getLoadPercent();
        }
        if (tasks.size() > 1) {
            allTasks += ",";
        }
        for (int i = 0; i < tasks.size() - 1; i++) {
            this.moveToNextTask();
            if (this.getCurrentTask().getType() == TaskType.LOAD) {
                allTasks += "LOAD@" + this.getCurrentTask().getLoadPercent();
                if (i != tasks.size() - 2) {
                    allTasks += ",";
                }
            } else {
                allTasks += this.getCurrentTask();
                if (i != tasks.size() - 2) {
                    allTasks += ",";
                }
            }
        }
        if (tasks.size() > 1) {
            this.moveToNextTask();
        }
        return allTasks;
    }
}
