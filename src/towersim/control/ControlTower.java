package towersim.control;

import towersim.aircraft.*;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;
import towersim.util.Tickable;

import java.util.*;

/**
 * Represents a the control tower of an airport.
 * <p>
 * The control tower is responsible for managing the operations of the airport, including arrivals
 * and departures in/out of the airport, as well as aircraft that need to be loaded with cargo
 * at gates in terminals.
 *
 * @ass1
 */
public class ControlTower implements Tickable {
    /**
     * List of all aircraft managed by the control tower.
     */
    private final List<Aircraft> aircraft;

    /**
     * List of all terminals in the airport.
     */
    private final List<Terminal> terminals;

    /**
     * The number of ticks that have elapsed for this control tower
     */
    private long ticksElapsed;

    /**
     * The queue of aircraft waiting to land
     */
    private LandingQueue landingQueue;

    /**
     * The queue of aircraft waiting to takeoff
     */
    private TakeoffQueue takeoffQueue;

    /**
     * The mapping of loading aircraft to their remaining load times
     */
    private Map<Aircraft, Integer> loadingAircraft;

    /**
     * An interger to calculate overall times that control tower ticks
     */
    private int tickTime;

    /**
     * Creates a new ControlTower.
     *
     * @param ticksElapsed    number of ticks that have elapsed since the tower
     *                        was first created
     * @param aircraft        list of aircraft managed by the control tower
     * @param landingQueue    queue of aircraft waiting to land
     * @param takeoffQueue    queue of aircraft waiting to take off
     * @param loadingAircraft mapping of aircraft that are loading cargo to the
     *                        number of ticks remaining for loading
     */
    public ControlTower(long ticksElapsed, List<Aircraft> aircraft,
                        LandingQueue landingQueue, TakeoffQueue takeoffQueue,
                        Map<Aircraft, Integer> loadingAircraft) {
        this.aircraft = aircraft;
        this.terminals = new ArrayList<>();
        this.ticksElapsed = ticksElapsed;
        this.landingQueue = landingQueue;
        this.takeoffQueue = takeoffQueue;
        this.loadingAircraft = loadingAircraft;
        this.tickTime = 0;
    }

    /**
     * Adds the given terminal to the jurisdiction of this control tower.
     *
     * @param terminal terminal to add
     * @ass1
     */
    public void addTerminal(Terminal terminal) {
        this.terminals.add(terminal);
    }

    /**
     * Returns a list of all terminals currently managed by this control tower.
     * <p>
     * The order in which terminals appear in this list should be the same as the order in which
     * they were added by calling {@link #addTerminal(Terminal)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all terminals
     * @ass1
     */
    public List<Terminal> getTerminals() {
        return new ArrayList<>(this.terminals);
    }

    /**
     * Adds the given aircraft to the jurisdiction of this control tower.
     * <p>
     * If the aircraft's current task type is {@code WAIT} or {@code LOAD}, it should be parked at a
     * suitable gate as found by the {@link #findUnoccupiedGate(Aircraft)} method.
     * If there is no suitable gate for the aircraft, the {@code NoSuitableGateException} thrown by
     * {@code findUnoccupiedGate()} should be propagated out of this method.
     *
     * @param aircraft aircraft to add
     * @throws NoSuitableGateException if there is no suitable gate for an aircraft with a current
     *                                 task type of {@code WAIT} or {@code LOAD}
     * @ass1
     */
    public void addAircraft(Aircraft aircraft) throws NoSuitableGateException {
        TaskType currentTaskType =
                aircraft.getTaskList().getCurrentTask().getType();
        if (currentTaskType == TaskType.WAIT
                || currentTaskType == TaskType.LOAD) {
            Gate gate = findUnoccupiedGate(aircraft);
            try {
                gate.parkAircraft(aircraft);
            } catch (NoSpaceException ignored) {
                // not possible, gate unoccupied
            }
        }
        this.aircraft.add(aircraft);
        placeAircraftInQueues(aircraft);
    }

    /**
     * Returns a list of all aircraft currently managed by this control tower.
     * <p>
     * The order in which aircraft appear in this list should be the same as the order in which
     * they were added by calling {@link #addAircraft(Aircraft)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all aircraft
     * @ass1
     */
    public List<Aircraft> getAircraft() {
        return new ArrayList<>(this.aircraft);
    }

    /**
     * Attempts to find an unoccupied gate in a compatible terminal for the given aircraft.
     * <p>
     * Only terminals of the same type as the aircraft's AircraftType (see
     * {@link towersim.aircraft.AircraftCharacteristics#type}) should be considered. For example,
     * for an aircraft with an AircraftType of {@code AIRPLANE}, only AirplaneTerminals may be
     * considered.
     * <p>
     * For each compatible terminal, the {@link Terminal#findUnoccupiedGate()} method should be
     * called to attempt to find an unoccupied gate in that terminal. If
     * {@code findUnoccupiedGate()} does not find a suitable gate, the next compatible terminal
     * in the order they were added should be checked instead, and so on.
     * <p>
     * If no unoccupied gates could be found across all compatible terminals, a
     * {@code NoSuitableGateException} should be thrown.
     *
     * @param aircraft aircraft for which to find gate
     * @return gate for given aircraft if one exists
     * @throws NoSuitableGateException if no suitable gate could be found
     * @ass1
     */
    public Gate findUnoccupiedGate(Aircraft aircraft)
            throws NoSuitableGateException {
        AircraftType aircraftType = aircraft.getCharacteristics().type;
        for (Terminal terminal : terminals) {
            /*
             * Only check for available gates at terminals that are of the same aircraft type as
             * the aircraft
             */
            if ((terminal instanceof AirplaneTerminal
                    && aircraftType == AircraftType.AIRPLANE)
                    || (terminal instanceof HelicopterTerminal
                    && aircraftType == AircraftType.HELICOPTER)) {
                if (!terminal.hasEmergency()) {
                    try {
                        // This terminal found a gate, return it
                        return terminal.findUnoccupiedGate();
                    } catch (NoSuitableGateException e) {
                        // If this terminal has no unoccupied gates, try the next one
                    }
                }
            }
        }
        throw new NoSuitableGateException("No gate available for aircraft");
    }

    /**
     * Finds the gate where the given aircraft is parked, and returns null if the aircraft is
     * not parked at any gate in any terminal.
     *
     * @param aircraft aircraft whose gate to find
     * @return gate occupied by the given aircraft; or null if none exists
     * @ass1
     */
    public Gate findGateOfAircraft(Aircraft aircraft) {
        for (Terminal terminal : this.terminals) {
            for (Gate gate : terminal.getGates()) {
                if (Objects.equals(gate.getAircraftAtGate(), aircraft)) {
                    return gate;
                }
            }
        }
        return null;
    }

    /**
     * Advances the simulation by one tick.
     * <p>
     * On each tick, the control tower should call {@link Aircraft#tick()} on all aircraft managed
     * by the control tower.
     * <p>
     * Note that the actions performed by {@code tick()} are very simple at the moment and will be
     * expanded on in assignment 2.
     *
     * @ass1
     */
    @Override
    public void tick() {
        // Call tick() on all other sub-entities
        tickTime += 1;
        for (Aircraft aircraft : this.aircraft) {
            aircraft.tick();
        }
        for (Aircraft aircraft : this.aircraft) {
            if (aircraft.getTaskList().getCurrentTask().getType()
                    == TaskType.AWAY
                    || aircraft.getTaskList().getCurrentTask().getType()
                    == TaskType.WAIT) {
                aircraft.getTaskList().moveToNextTask();
            }
        }
        loadAircraft();
        if (tickTime % 2 == 0) {
            if (!tryLandAircraft()) {
                tryTakeOffAircraft();
            }
        } else {
            tryTakeOffAircraft();
        }
        placeAllAircraftInQueues();
        this.ticksElapsed += 1;
    }

    /**
     * Returns the number of ticks that have elapsed for this control tower.
     *
     * @return number of ticks elapsed
     */
    public long getTicksElapsed() {
        return this.ticksElapsed;
    }

    /**
     * Returns the queue of aircraft waiting to land.
     *
     * @return landing queue
     */
    public AircraftQueue getLandingQueue() {
        return this.landingQueue;
    }

    /**
     * Returns the queue of aircraft waiting to take off.
     *
     * @return takeoff queue
     */
    public AircraftQueue getTakeoffQueue() {
        return this.takeoffQueue;
    }

    /**
     * Returns the mapping of loading aircraft to their remaining load times.
     *
     * @return loading aircraft map
     */
    public Map<Aircraft, Integer> getLoadingAircraft() {
        return this.loadingAircraft;
    }

    /**
     * Moves the given aircraft to the appropriate queue based on its current
     * task.
     *
     * @param aircraft aircraft to move to appropriate queue
     */
    public void placeAircraftInQueues(Aircraft aircraft) {
        if (aircraft.getTaskList().getCurrentTask().getType()
                == TaskType.LAND) {
            if (!this.landingQueue.containsAircraft(aircraft)) {
                this.landingQueue.addAircraft(aircraft);
            }
        }
        if (aircraft.getTaskList().getCurrentTask().getType()
                == TaskType.TAKEOFF) {
            if (!this.takeoffQueue.containsAircraft(aircraft)) {
                this.takeoffQueue.addAircraft(aircraft);
            }
        }
        if (aircraft.getTaskList().getCurrentTask().getType()
                == TaskType.LOAD) {
            if (!this.loadingAircraft.containsKey(aircraft)) {
                this.loadingAircraft.put(aircraft,
                        aircraft.getLoadingTime());
            }
        }
    }

    /**
     * Calls placeAircraftInQueues(Aircraft) on all aircraft managed by the
     * control tower.
     */
    public void placeAllAircraftInQueues() {
        for (Aircraft aircraft : this.getAircraft()) {
            placeAircraftInQueues(aircraft);
        }
    }

    /**
     * Attempts to land one aircraft waiting in the landing queue and park it
     * at a suitable gate.
     *
     * @return true if an aircraft was successfully landed and parked; false
     * otherwise
     */
    public boolean tryLandAircraft() {
        if (this.landingQueue.getAircraftInOrder().isEmpty()) {
            return false;
        } else {
            try {
                findUnoccupiedGate(
                        this.landingQueue.getAircraftInOrder().get(0))
                        .parkAircraft(
                                this.landingQueue.getAircraftInOrder().get(0));
            } catch (NoSuitableGateException | NoSpaceException e) {
                return false;
            }
            this.landingQueue.getAircraftInOrder().get(0).unload();
            this.landingQueue.getAircraftInOrder().get(0).getTaskList()
                    .moveToNextTask();
            this.landingQueue.removeAircraft();
        }
        return true;
    }

    /**
     * Attempts to allow one aircraft waiting in the takeoff queue to take off.
     */
    public void tryTakeOffAircraft() {
        if (this.takeoffQueue.getAircraftInOrder().isEmpty()) {
            return;
        } else {
            this.takeoffQueue.getAircraftInOrder().get(0).getTaskList()
                    .moveToNextTask();
            this.takeoffQueue.removeAircraft();
        }
    }

    /**
     * Updates the time remaining to load on all currently loading aircraft and
     * removes aircraft from their gate once finished loading.
     */
    public void loadAircraft() {
        for (Aircraft aircraft1 : this.aircraft) {
            if (!this.loadingAircraft.isEmpty()) {
                if (this.loadingAircraft.containsKey(aircraft1)) {
                    this.loadingAircraft.replace(aircraft1,
                            this.loadingAircraft.get(aircraft1) - 1);
                }
            }
        }
        for (Aircraft aircraft1 : this.aircraft) {
            if (!this.loadingAircraft.isEmpty()) {
                if (this.loadingAircraft.containsKey(aircraft1)) {
                    if (this.loadingAircraft.get(aircraft1) <= 0) {
                        this.loadingAircraft.remove(aircraft1);
                        this.findGateOfAircraft(aircraft1).aircraftLeaves();
                        aircraft1.getTaskList().moveToNextTask();
                    }
                }
            }
        }
    }

    /**
     * Returns the human-readable string representation of this control tower.
     *
     * @return string representation of this control tower
     */
    @Override
    public String toString() {
        String controlTower = "ControlTower: ";
        controlTower +=
                this.getTerminals().size() + " terminals, " + this.getAircraft()
                        .size() + " total aircraft ";
        List<Aircraft> landingAir = new ArrayList<>();
        List<Aircraft> takingOffAir = new ArrayList<>();
        List<Aircraft> loadingAir = new ArrayList<>();
        for (Aircraft aircraft : this.getAircraft()) {
            if (aircraft.getTaskList().getCurrentTask().getType()
                    == TaskType.LAND) {
                landingAir.add(aircraft);
            }
            if (aircraft.getTaskList().getCurrentTask().getType()
                    == TaskType.TAKEOFF) {
                takingOffAir.add(aircraft);
            }
            if (aircraft.getTaskList().getCurrentTask().getType()
                    == TaskType.LOAD) {
                loadingAir.add(aircraft);
            }
        }
        int numLanding = landingAir.size();
        int numTakeoff = takingOffAir.size();
        int numLoad = loadingAir.size();
        controlTower += "(" + numLanding + " LAND, " + numTakeoff + " TAKEOFF, "
                + numLoad + " LOAD)";
        return controlTower;
    }
}
