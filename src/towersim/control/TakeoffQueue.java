package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a first-in-first-out (FIFO) queue of aircraft waiting to take off.
 */
public class TakeoffQueue extends AircraftQueue {

    /**
     * A list of aircraft represents the landing queue.
     */
    private final List<Aircraft> aircrafts;

    /**
     * Constructs a new TakeoffQueue with an initially empty queue of aircraft.
     */
    public TakeoffQueue() {
        this.aircrafts = new ArrayList<>();
    }

    /**
     * Adds the given aircraft to the queue.
     *
     * @param aircraft aircraft to add to queue
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        aircrafts.add(aircraft);
    }

    /**
     * Removes and returns the aircraft at the front of the queue. Returns null
     * if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft removeAircraft() {
        if (aircrafts.isEmpty()) {
            return null;
        } else {
            List<Aircraft> frontAircraft = new ArrayList<>();
            frontAircraft.add(aircrafts.get(0));
            aircrafts.remove(aircrafts.get(0));
            return frontAircraft.get(0);
        }
    }

    /**
     * Returns the aircraft at the front of the queue without removing it from
     * the queue, or null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft peekAircraft() {
        if (aircrafts.isEmpty()) {
            return null;
        } else {
            return aircrafts.get(0);
        }
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     *
     * @return list of all aircraft in queue, in queue order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        return new ArrayList<>(aircrafts);
    }

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    @Override
    public boolean containsAircraft(Aircraft aircraft) {
        if (aircrafts.contains(aircraft)) {
            return true;
        }
        return false;
    }
}
