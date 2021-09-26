package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.util.Encodable;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract representation of a queue containing aircraft.
 */
public abstract class AircraftQueue extends Object implements Encodable {
    /**
     * Constructs an abstract AircraftQueue.
     */
    public AircraftQueue() {
    }

    /**
     * Adds the given aircraft to the queue.
     *
     * @param aircraft aircraft to add to queue
     */
    public abstract void addAircraft(Aircraft aircraft);

    /**
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public abstract Aircraft removeAircraft();

    /**
     * Returns the aircraft at the front of the queue without removing it from
     * the queue, or null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public abstract Aircraft peekAircraft();

    /**
     * Returns a list containing all aircraft in the queue, in order.
     *
     * @return list of all aircraft in queue, in queue order
     */
    public abstract List<Aircraft> getAircraftInOrder();

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    public abstract boolean containsAircraft(Aircraft aircraft);

    /**
     * Returns the human-readable string representation of this aircraft queue.
     *
     * @return string representation of this queue
     */
    public String toString() {
        String queue = "";
        queue += this.getClass().getSimpleName();
        if (this.getAircraftInOrder().isEmpty()) {
            queue += " []";
        } else {
            queue += " [";
            int pos = 0;
            for (Aircraft aircraft : this.getAircraftInOrder()) {
                queue += aircraft.getCallsign();
                pos += 1;
                if (pos < this.getAircraftInOrder().size()) {
                    queue += ", ";
                }
            }
            queue += "]";
        }
        return queue;
    }

    /**
     * Returns the machine-readable string representation of this aircraft
     * queue.
     *
     * @return encoded string representation of this aircraft queue
     */
    public String encode() {
        String queue = "";
        if (this.getAircraftInOrder().isEmpty()) {
            queue += this.getClass().getSimpleName() + ":0";
        } else {
            queue += this.getClass().getSimpleName() + ":" + this
                    .getAircraftInOrder().size();
            queue += System.lineSeparator();
            int pos = 0;
            for (Aircraft aircraft : this.getAircraftInOrder()) {
                queue += aircraft.getCallsign();
                pos += 1;
                if (pos < this.getAircraftInOrder().size()) {
                    queue += ",";
                }
            }
        }
        return queue;
    }
}
