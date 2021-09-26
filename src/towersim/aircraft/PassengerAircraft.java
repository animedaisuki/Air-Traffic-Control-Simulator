package towersim.aircraft;

import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an aircraft capable of carrying passenger cargo.
 *
 * @ass1
 */
public class PassengerAircraft extends Aircraft {

    /**
     * Average weight of a single passenger including their baggage, in kilograms.
     *
     * @ass1
     */
    public static final double AVG_PASSENGER_WEIGHT = 90;

    /**
     * Number of passengers currently onboard the aircraft
     */
    private int numPassengers;

    /**
     * Creates a new passenger aircraft with the given callsign, task list, fuel capacity, amount of
     * fuel and number of passengers.
     * <p>
     * If the given number of passengers is less than zero or greater than the aircraft's maximum
     * passenger capacity as defined in the aircraft's characteristics, then an
     * {@code IllegalArgumentException} should be thrown.
     *
     * @param callsign        unique callsign
     * @param characteristics characteristics that describe this aircraft
     * @param tasks           task list to be used by aircraft
     * @param fuelAmount      current amount of fuel onboard, in litres
     * @param numPassengers   current number of passengers onboard
     * @throws IllegalArgumentException if numPassengers &lt; 0 or if numPassengers &gt; passenger
     *                                  capacity
     * @ass1
     */
    public PassengerAircraft(String callsign,
                             AircraftCharacteristics characteristics,
                             TaskList tasks, double fuelAmount,
                             int numPassengers) {
        super(callsign, characteristics, tasks, fuelAmount);

        if (numPassengers < 0) {
            throw new IllegalArgumentException(
                    "Number of passengers onboard cannot be negative");
        }
        if (numPassengers > characteristics.passengerCapacity) {
            throw new IllegalArgumentException(
                    "Number of passengers onboard cannot exceed "
                            + "capacity");
        }

        this.numPassengers = numPassengers;
    }

    /**
     * Returns the total weight of the aircraft in its current state.
     * <p>
     * The total weight for a passenger aircraft is calculated as the sum of:
     * <ul>
     * <li>the aircraft's empty weight</li>
     * <li>the amount of fuel onboard the aircraft multiplied by the weight of a litre of fuel</li>
     * <li>the number of passengers onboard multiplied by the weight of an average passenger,
     * including baggage</li>
     * </ul>
     *
     * @return total weight of aircraft in kilograms
     * @ass1
     */
    @Override
    public double getTotalWeight() {
        return super.getTotalWeight()
                + this.numPassengers * AVG_PASSENGER_WEIGHT;
    }

    /**
     * Returns the number of ticks required to load the aircraft at the gate.
     * <p>
     * The loading time for passenger aircraft is calculated as the logarithm (base 10) of the
     * number of passengers to be loaded, rounded to the nearest integer. Note that the loading time
     * is bounded below by 1, that is, if the result of this calculation gives a number less than 1,
     * then 1 should be returned instead.
     * <p>
     * The number of passengers to be loaded is equal to the maximum passenger capacity of the
     * aircraft multiplied by the load ratio specified in the aircraft's current task (see
     * {@link towersim.tasks.Task#getLoadPercent()}). The result of this calculation should be
     * rounded to the nearest whole passenger.
     * <p>
     * For example, suppose an aircraft has a capacity of 175 passengers and its current task is a
     * LOAD task with a load percentage of 65%. The number of passengers to load would be 114
     * (rounded from 113.75, which is 65% of 175). Then, the loading time would be
     * {@code log(114) = 2.057} rounded to <b>2 ticks</b>.
     *
     * @return loading time in ticks
     * @ass1
     */
    @Override
    public int getLoadingTime() {
        return (int) Math
                .max(1, Math.round(Math.log10(this.getPassengersToLoad())));
    }

    /**
     * Returns the ratio of passengers onboard to maximum passenger capacity as a
     * percentage between 0 and 100.
     * <p>
     * 0 represents no passengers onboard, and 100 represents the aircraft being at maximum capacity
     * of passengers onboard.
     * <p>
     * The calculated value should be rounded to the nearest percentage point.
     *
     * @return occupancy level as a percentage
     * @ass1
     */
    @Override
    public int calculateOccupancyLevel() {
        return (int) Math.round((double) this.numPassengers * 100
                / this.getCharacteristics().passengerCapacity);
    }

    /**
     * Returns the total number of passengers to be loaded onto the aircraft based on the current
     * task's load percentage.
     *
     * @return total number of passengers to be loaded
     * @ass1
     */
    private int getPassengersToLoad() {
        int passengerCapacity = this.getCharacteristics().passengerCapacity;
        double loadRatio =
                (double) this.getTaskList().getCurrentTask().getLoadPercent()
                        / 100;
        return (int) Math.round(passengerCapacity * loadRatio);
    }

    /**
     * Updates the aircraft's state on each tick of the simulation.
     * <p>
     * Firstly, the {@link Aircraft#tick()} method in the superclass should be called to perform
     * refueling and burning of fuel.
     * <p>
     * Next, if the aircraft's current task is a {@code LOAD} task, passengers should be loaded onto
     * the aircraft. The number of passengers to load in a single call of {@code tick()} is equal to
     * the total number of passengers to be loaded based on the {@code LOAD} task's load percentage,
     * divided by the loading time given by {@link #getLoadingTime()}. This ensures that passengers
     * are loaded in equal increments across the entire loading time. The result of this division
     * operation may yield a number of passengers that is not an integer, in which case it should be
     * rounded to the nearest whole integer (whole passenger).
     * <p>
     * Note that the total number of passengers on the aircraft should not be allowed to exceed the
     * maximum passenger capacity of the aircraft, given by
     * {@link AircraftCharacteristics#passengerCapacity}.
     * <p>
     * For example, suppose an aircraft initially has 0 passengers onboard and has a current task
     * of type {@code LOAD} with a load percentage of 45%. The aircraft has a passenger capacity of
     * 150. Then, the total number of passengers to be loaded is 45% of 150 = 67.5 rounded to 68.
     * According to {@link #getLoadingTime()}, this number of passengers will take 2 ticks to load.
     * So, a single call to {@code tick()} should increase the number of passengers onboard by
     * 68 / 2 = 34.
     *
     * @ass1
     */
    @Override
    public void tick() {
        super.tick();

        if (this.getTaskList().getCurrentTask().getType() == TaskType.LOAD) {
            int paxToLoadThisTick = (int) Math.round(this.getPassengersToLoad()
                    / (double) this.getLoadingTime());
            this.numPassengers =
                    Math.min(this.numPassengers + paxToLoadThisTick,
                            this.getCharacteristics().passengerCapacity);
        }
    }

    /**
     * Unloads the aircraft of all cargo (passengers/freight) it is currently
     * carrying.
     */
    @Override
    public void unload() {
        this.numPassengers = 0;
    }

    /**
     * Returns the machine-readable string representation of this passenger
     * aircraft.
     *
     * @return encoded string representation of this aircraft
     */
    public String encode() {
        return super.encode() + ":" + this.numPassengers;
    }
}
