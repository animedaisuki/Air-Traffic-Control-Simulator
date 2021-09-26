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
 * Represents a rule-based queue of aircraft waiting in the air to land.
 */
public class LandingQueue extends AircraftQueue {

    /**
     * A list of aircraft represents the landing queue.
     */
    private List<Aircraft> aircrafts;

    /**
     * Constructs a new LandingQueue with an initially empty queue of aircraft.
     */
    public LandingQueue() {
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
     * Returns the aircraft at the front of the queue without removing it from
     * the queue, or null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft removeAircraft() {
        if (aircrafts.isEmpty()) {
            return null;
        } else {
            List<Aircraft> frontAircraft = new ArrayList<>();
            frontAircraft.add(peekAircraft());
            aircrafts.remove(peekAircraft());
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
            List<Aircraft> emergencyAircrafts = new ArrayList<>();
            List<Aircraft> pickEmergencyAir = new ArrayList<>();
            List<Aircraft> fuelPercentAircrafts = new ArrayList<>();
            List<Aircraft> pickFuelPercentAir = new ArrayList<>();
            List<Aircraft> passengerAircrafts = new ArrayList<>();
            List<Aircraft> pickPassengerAir = new ArrayList<>();
            List<Aircraft> allAircrafts = new ArrayList<>();
            for (Aircraft aircraft : aircrafts) {
                if (aircraft.hasEmergency()) {
                    emergencyAircrafts.add(aircraft);
                }
                if (aircraft.getFuelPercentRemaining() <= 20) {
                    fuelPercentAircrafts.add(aircraft);
                }
                if (aircraft.getCharacteristics().passengerCapacity > 0) {
                    passengerAircrafts.add(aircraft);
                }
            }
            try {
                pickEmergencyAir.add(emergencyAircrafts.get(0));
            } catch (IndexOutOfBoundsException e) {
                //ignored
            }
            try {
                pickFuelPercentAir.add(fuelPercentAircrafts.get(0));
            } catch (IndexOutOfBoundsException e) {
                //ignored
            }
            try {
                pickPassengerAir.add(passengerAircrafts.get(0));
            } catch (IndexOutOfBoundsException e) {
                //ignored
            }
            if (!pickEmergencyAir.isEmpty()) {
                allAircrafts.add(pickEmergencyAir.get(0));
            }
            if (!pickFuelPercentAir.isEmpty()) {
                allAircrafts.add(pickFuelPercentAir.get(0));
            }
            if (!pickPassengerAir.isEmpty()) {
                allAircrafts.add(pickPassengerAir.get(0));
            }
            if (allAircrafts.isEmpty()) {
                return aircrafts.get(0);
            } else {
                if (!pickEmergencyAir.isEmpty()) {
                    return pickEmergencyAir.get(0);
                }
                if (!pickFuelPercentAir.isEmpty()) {
                    return pickFuelPercentAir.get(0);
                }
                if (!pickPassengerAir.isEmpty()) {
                    return pickPassengerAir.get(0);
                }
                return this.aircrafts.get(0);
            }
        }
//        if (aircrafts.isEmpty()){
//            return null;
//        } else {
//            for (Aircraft aircraft : aircrafts){
//                if (aircraft.hasEmergency()){
//                    return aircraft;
//                }
//                if (aircraft.getFuelPercentRemaining() <= 20){
//                    return aircraft;
//                }
//                if (aircraft.getCharacteristics().passengerCapacity > 0){
//                    return aircraft;
//                }
//            }
//        }
//        return aircrafts.get(0);
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     *
     * @return list of all aircraft in queue, in queue order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        List<Aircraft> aircraftsCopy = new ArrayList<>();
        List<Aircraft> aircraftInOrder = new ArrayList<>();
        for (Aircraft aircraft : this.aircrafts) {
            aircraftsCopy.add(aircraft);
        }
        for (int i = 0; i < aircraftsCopy.size(); i++) {
            aircraftInOrder.add(peekAircraft());
            this.removeAircraft();
        }
        this.aircrafts = aircraftsCopy;
        return aircraftInOrder;
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
