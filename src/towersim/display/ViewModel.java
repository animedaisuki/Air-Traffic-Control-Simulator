package towersim.display;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import towersim.aircraft.Aircraft;
import towersim.control.ControlTower;
import towersim.control.ControlTowerInitialiser;
import towersim.ground.Gate;
import towersim.ground.Terminal;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;
import towersim.util.NoSuitableGateException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * View model for the Control Tower Simulation GUI.
 *
 * @ass2
 */
public class ViewModel {
    /**
     * Control tower model containing aircraft and terminals
     */
    private final ControlTower tower;

    /**
     * Whether the state of the model has changed
     */
    private final BooleanProperty changed = new SimpleBooleanProperty(false);

    /**
     * Contents of aircraft information text box
     */
    private final StringProperty aircraftInfoText = new SimpleStringProperty(
            "No aircraft selected");

    /**
     * Contents of loading aircraft information text box
     */
    private final StringProperty loadingInfoText = new SimpleStringProperty("");

    /**
     * Whether the simulation is paused or not
     */
    private final BooleanProperty paused = new SimpleBooleanProperty(true);

    /**
     * Text appended to window title when the simulation is paused
     */
    private final StringProperty pausedStatusText =
            new SimpleStringProperty(" (Paused)");

    /**
     * Text displayed in the "toggle pause" menu item
     */
    private final StringProperty pauseMenuText =
            new SimpleStringProperty("Un_pause");

    /**
     * Number of terminals managed by the control tower
     */
    private final IntegerProperty numTerminals = new SimpleIntegerProperty();

    /**
     * Text displayed in the label showing the suitable gate for landing aircraft
     */
    private final StringProperty suitableGateText =
            new SimpleStringProperty("");

    /**
     * The currently selected (clicked) aircraft
     */
    private final ObjectProperty<Aircraft> selectedAircraft =
            new SimpleObjectProperty<>();

    /**
     * The aircraft currently landing (i.e. just went from LAND to WAIT/LOAD)
     */
    private final ObjectProperty<Aircraft> aircraftLanding =
            new SimpleObjectProperty<>();

    /**
     * The aircraft currently taking off (i.e. just went from TAKEOFF to AWAY)
     */
    private final ObjectProperty<Aircraft> aircraftTakingOff =
            new SimpleObjectProperty<>();

    /**
     * List of all aircraft whose task is TAKEOFF; used in finding aircraftTakingOff
     */
    private List<Aircraft> allTakeoffAircraft = new ArrayList<>();

    /**
     * List of all aircraft whose task is LAND; used in finding aircraftLanding
     */
    private List<Aircraft> allLandAircraft = new ArrayList<>();

    /**
     * File path of the tick file that we loaded from
     */
    private final String defaultTickSaveLocation;

    /**
     * File path of the aircraft file that we loaded from
     */
    private final String defaultAircraftSaveLocation;

    /**
     * File path of the queues file that we loaded from
     */
    private final String defaultQueuesSaveLocation;

    /**
     * File path of the terminals with gates file that we loaded from
     */
    private final String defaultTerminalsSaveLocation;

    /**
     * Creates a new view model and constructs a control tower by reading from the given filenames.
     *
     * @param filenames list of four filenames, specifying the paths to: (1) the tick file;
     *                  (2) the aircraft file; (3) the queues file; (4) the terminals/gates file
     * @throws IOException            if loading from the files specifies generates an IOException
     * @throws MalformedSaveException if any of the files are invalid according to
     *                                {@link ControlTowerInitialiser
     *                                #createControlTower
     *                                (Reader, Reader, Reader, Reader)}
     * @requires filenames != null &amp;&amp; filenames.size() == 4
     * @given
     */
    public ViewModel(List<String> filenames)
            throws IOException, MalformedSaveException {
        this.defaultTickSaveLocation = filenames.get(0);
        this.defaultAircraftSaveLocation = filenames.get(1);
        this.defaultQueuesSaveLocation = filenames.get(2);
        this.defaultTerminalsSaveLocation = filenames.get(3);

        this.tower = ControlTowerInitialiser.createControlTower(
                new FileReader(filenames.get(0)),
                new FileReader(filenames.get(1)),
                new FileReader(filenames.get(2)),
                new FileReader(filenames.get(3)));

        this.numTerminals.set(tower.getTerminals().size());

        this.selectedAircraft.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                aircraftInfoText.set("No aircraft selected");
            } else {
                aircraftInfoText.set(generateAircraftInfoText(newValue));
            }
        });
        this.loadingInfoText.set(generateLoadingInfoText());

        fillTakeoffLandAircraftLists();
    }

    /**
     * Returns an event handler for when the "Drone Alert" button is clicked.
     * <p>
     * This event handler should declare a state of emergency on all terminals managed by the
     * control tower. Finally, it should call {@link #registerChange()} to update the GUI.
     *
     * @return event handler for "Drone Alert" button
     * @ass2
     */
    public EventHandler<ActionEvent> getDroneAlertHandler() {
        EventHandler<ActionEvent> eventEventHandler =
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        for (Terminal terminal : tower.getTerminals()) {
                            terminal.declareEmergency();
                        }
                        registerChange();
                    }
                };
        return eventEventHandler; // TODO implement for assignment 2
    }

    /**
     * Returns an event handler for when the "Clear Drone Alert" button is clicked.
     * <p>
     * This event handler should clear the state of emergency on all terminals managed by
     * control tower. Finally, it should all {@link #registerChange()} to update the GUI.
     *
     * @return event handler for "Clear Drone Alert" button
     * @ass2
     */
    public EventHandler<ActionEvent> getDroneClearHandler() {
        EventHandler<ActionEvent> eventEventHandler =
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        for (Terminal terminal : tower.getTerminals()) {
                            terminal.clearEmergency();
                        }
                        registerChange();
                    }
                };
        return eventEventHandler; // TODO implement for assignment 2
    }

    /**
     * Returns an event handler for when the "Find Gate for Selected Aircraft" button is clicked.
     * <p>
     * The overall purpose of this event handler is to update the {@code suitableGateText} string
     * property with the gate found for the currently selected aircraft, as returned by
     * {@link ControlTower#findUnoccupiedGate(Aircraft)}.
     * <p>
     * The event handler should perform the following actions:<ol>
     * <li>If no aircraft is currently selected (i.e. {@link #getSelectedAircraft()} is storing
     * null), then the event handler should return immediately without taking any further action.
     * </li>
     * <li>If the currently selected aircraft's current task type is not {@code LAND}, then the
     * event handler should return immediately without taking any further action.
     * </li>
     * <li>If calling {@link ControlTower#findUnoccupiedGate(Aircraft)} for the
     * currently selected aircraft throws a NoSuitableGateException, then the
     * {@code suitableGateText} property should be set to {@code "NoSuitableGateException"}.</li>
     * <li>Otherwise, the {@code suitableGateText} property should be set to the
     * {@link Gate#toString() toString()} representation of the gate found by
     * {@link ControlTower#findUnoccupiedGate(Aircraft) findUnoccupiedGate()}.</li>
     * </ol>
     *
     * @return event handler for "Find Gate for Selected Aircraft" button
     * @ass2
     */
    public EventHandler<ActionEvent> getFindSuitableGateHandler() {
        EventHandler<ActionEvent> eventEventHandler =
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        if (getSelectedAircraft().get() != null) {
                            if (getSelectedAircraft().get().getTaskList()
                                    .getCurrentTask().getType()
                                    == TaskType.LAND) {
                                try {
                                    tower.findUnoccupiedGate(
                                            getSelectedAircraft().get());
                                    suitableGateText.setValue(
                                            tower.findUnoccupiedGate(
                                                    getSelectedAircraft()
                                                            .get()).toString());
                                } catch (NoSuitableGateException e) {
                                    suitableGateText.setValue(
                                            "NoSuitableGateException");
                                }
                            }
                        }
                    }
                };
        return eventEventHandler; // TODO implement for assignment 2
    }

    /**
     * Saves the current state of the control tower simulation to the given writers.
     * <p>
     * Each writer should be written to in the following format:
     * <table border="1"><caption>Writer output format</caption>
     * <tr><th>Writer</th><th>Format</th></tr>
     * <tr><td>{@code tickWriter}</td><td><pre>
     * ticksElapsed
     * </pre> where {@code ticksElapsed} is the number of ticks elapsed, as returned by
     * {@link ControlTower#getTicksElapsed()}
     * </td></tr>
     * <tr><td>{@code aircraftWriter}</td><td><pre>
     * numAircraft
     * encodedAircraft1
     * encodedAircraft2
     * ...
     * encodedAircraftN
     * </pre> where <ul>
     * <li>{@code numAircraft} is the number of aircraft managed by the control tower</li>
     * <li>{@code encodedAircraftX} is the encoded representation of the X<sup>th</sup> aircraft
     * managed by the control tower, in the same order as returned by
     * {@link ControlTower#getAircraft()}, with X from 1 to N inclusive, where N is the total
     * number of aircraft</li>
     * </ul></td></tr>
     * <tr><td>{@code queuesWriter}</td><td><pre>
     * encodedTakeoffQueue
     * encodedLandingQueue
     * LoadingAircraft:numLoadingAircraft
     * callsign1:ticksRemaining1,callsign2:ticksRemaining2,...,callsignN:ticksRemainingN
     * </pre> where <ul>
     * <li>{@code encodedTakeoffQueue} is the encoded representation of the control tower's takeoff
     * queue</li>
     * <li>{@code encodedLandingQueue} is the encoded representation of the control tower's landing
     * queue</li>
     * <li>{@code numLoadingAircraft} is the number of aircraft currently loading at a gate</li>
     * <li>{@code callsignX} is the callsign of the X<sup>th</sup> aircraft that is loading at a
     * gate, in the same order as returned by {@link ControlTower#getLoadingAircraft()}, with X
     * from 1 to N inclusive, where N is the number of aircraft currently loading</li>
     * <li>{@code ticksRemainingX} is the number of ticks remaining in the loading process of the
     * X<sup>th</sup> aircraft that is loading at a gate, in the same order as returned by
     * {@link ControlTower#getLoadingAircraft()}, with X from 1 to N inclusive, where N is the
     * number of aircraft currently loading</li>
     * </ul></td></tr>
     * <tr><td>{@code terminalsWithGatesWriter}</td><td><pre>
     * numTerminals
     * encodedTerminalWithGates1
     * encodedTerminalWithGates2
     * ...
     * encodedTerminalWithGatesN
     * </pre> where
     * <ul>
     * <li>{@code numTerminals} is the number of terminals managed by the control tower</li>
     * <li>{@code encodedTerminalWithGatesX} is the encoded representation of the X<sup>th</sup>
     * terminal (including its gates) in the same order as returned by
     * {@link ControlTower#getTerminals()}, with X between 1 and N inclusive, where N is the
     * number of terminals.
     * </li>
     * </ul></td></tr></table>
     *
     * @param tickWriter               writer to which the number of ticks elapsed will be written
     * @param aircraftWriter           writer to which the list of aircraft will be written
     * @param queuesWriter             writer to which the takeoff/landing
     *                                 queues and loading map will be written
     * @param terminalsWithGatesWriter writer to which the list of terminals and their gates will
     *                                 be written
     * @throws IOException if an IOException occurs when writing to the writers
     * @ass2
     */
    public void saveAs(Writer tickWriter, Writer aircraftWriter,
                       Writer queuesWriter,
                       Writer terminalsWithGatesWriter) throws IOException {
        BufferedWriter writeTick = new BufferedWriter(tickWriter);
        BufferedWriter writeAircraft = new BufferedWriter(aircraftWriter);
        writeTick.write(String.valueOf(tower.getTicksElapsed()));
        writeTick.flush();
        List<Aircraft> aircrafts = new ArrayList<>(tower.getAircraft());
        writeAircraft.write(String.valueOf(aircrafts.size()));
        if (aircrafts.size() > 0) {
            writeAircraft.newLine();
            int countForAircrafts = 0;
            for (Aircraft aircraft : aircrafts) {
                writeAircraft.write(aircraft.encode());
                countForAircrafts += 1;
                if (countForAircrafts < aircrafts.size()) {
                    writeAircraft.newLine();
                }
            }
        }
        writeAircraft.flush();
        BufferedWriter writeQueue = new BufferedWriter(queuesWriter);
        writeQueue.write(tower.getTakeoffQueue().encode());
        writeQueue.newLine();
        writeQueue.write(tower.getLandingQueue().encode());
        writeQueue.newLine();
        writeQueue
                .write("LoadingAircraft:" + tower.getLoadingAircraft().size());
        int countForMap = 0;
        if (tower.getLoadingAircraft().size() > 0) {
            writeQueue.newLine();
            for (Aircraft aircraft : tower.getLoadingAircraft().keySet()) {
                countForMap += 1;
                writeQueue.write(aircraft.getCallsign() + ":" + tower
                        .getLoadingAircraft().get(aircraft));
                if (countForMap < tower.getLoadingAircraft().size()) {
                    writeQueue.write(",");
                }
            }
        }
        writeQueue.flush();
        BufferedWriter writeTerminal =
                new BufferedWriter(terminalsWithGatesWriter);
        List<Terminal> terminals = new ArrayList<>(tower.getTerminals());
        writeTerminal.write(String.valueOf(terminals.size()));
        if (terminals.size() > 0) {
            writeTerminal.newLine();
            int countForTerminals = 0;
            for (Terminal terminal : terminals) {
                writeTerminal.write(terminal.encode());
                countForTerminals += 1;
                if (countForTerminals < terminals.size()) {
                    writeTerminal.newLine();
                }
            }
        }
        writeTerminal.flush();
    }

    /**
     * Returns the control tower linked to this view model.
     *
     * @return control tower
     * @given
     */
    public ControlTower getControlTower() {
        return tower;
    }

    /**
     * Ticks the model and updates the state of the GUI.
     *
     * @given
     */
    public void tick() {
        tower.tick();
        this.loadingInfoText.set(generateLoadingInfoText());
        if (selectedAircraft.isNotNull().get()) {
            this.aircraftInfoText
                    .set(generateAircraftInfoText(selectedAircraft.get()));
        }
        updateTakeoffLandAircraft();
        registerChange();
    }

    /* Updates the lists of aircraft currently taking off and landing */
    private void updateTakeoffLandAircraft() {
        this.aircraftTakingOff.set(null);
        this.aircraftLanding.set(null);
        for (Aircraft aircraft : getControlTower().getAircraft()) {
            TaskType currentTaskType =
                    aircraft.getTaskList().getCurrentTask().getType();
            if (currentTaskType == TaskType.AWAY && allTakeoffAircraft
                    .contains(aircraft)) {
                // Aircraft has just taken off
                this.aircraftTakingOff.set(aircraft);
            }
            if ((currentTaskType == TaskType.WAIT
                    || currentTaskType == TaskType.LOAD)
                    && allLandAircraft.contains(aircraft)) {
                // Aircraft has just landed
                this.aircraftLanding.set(aircraft);
            }
        }
        fillTakeoffLandAircraftLists();
    }

    /*
     * Places all aircraft that have a TAKEOFF task into the list of aircraft taking off;
     * same for LAND
     */
    private void fillTakeoffLandAircraftLists() {
        this.allTakeoffAircraft =
                findAircraftWithTask(getControlTower().getAircraft(),
                        TaskType.TAKEOFF);
        this.allLandAircraft =
                findAircraftWithTask(getControlTower().getAircraft(),
                        TaskType.LAND);
    }

    /* Returns all aircraft in the given list whose current task's type is the given type */
    private List<Aircraft> findAircraftWithTask(List<Aircraft> aircraft,
                                                TaskType taskType) {
        return aircraft.stream()
                .filter(a -> a.getTaskList().getCurrentTask().getType()
                        == taskType)
                .collect(Collectors.toList());
    }

    /* Generates the formatted information text for the given aircraft */
    private String generateAircraftInfoText(Aircraft aircraft) {
        StringJoiner lineJoiner = new StringJoiner(System.lineSeparator());
        lineJoiner.add("Currently selected aircraft:");
        lineJoiner.add("Callsign:       \t" + aircraft.getCallsign());
        lineJoiner.add("Cargo type:\t" + aircraft.getClass().getSimpleName());
        lineJoiner.add("Aircraft type:\t" + aircraft.getCharacteristics().type);
        lineJoiner.add("Model:          \t" + aircraft.getCharacteristics()
                .name());
        lineJoiner.add("Fuel % left:\t" + aircraft.getFuelPercentRemaining()
                + "%");
        lineJoiner.add("% occupied:\t" + aircraft.calculateOccupancyLevel()
                + "%");
        lineJoiner.add("Emergency:\t" + aircraft.hasEmergency());
        lineJoiner.add("Task list:   \t" + aircraft.getTaskList().encode());

        return lineJoiner.toString();
    }

    /* Generates the formatted information text for the map of loading aircraft */
    private String generateLoadingInfoText() {
        Map<Aircraft, Integer> loadingAircraft =
                this.getControlTower().getLoadingAircraft();
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("Loading aircraft: " + (loadingAircraft.isEmpty() ? "none"
                : ""));
        for (Map.Entry<Aircraft, Integer> entry : loadingAircraft.entrySet()) {
            Aircraft aircraft = entry.getKey();
            joiner.add(aircraft.getCallsign() + " at gate "
                    + tower.findGateOfAircraft(aircraft).getGateNumber() + ":\t"
                    + entry.getValue().toString()
                    + " ticks remaining (currently at " + aircraft
                    .calculateOccupancyLevel()
                    + "%, will load to " + aircraft.getTaskList()
                    .getCurrentTask().getLoadPercent()
                    + "%)");
        }
        return joiner.toString();
    }

    /**
     * Toggles whether the simulation is paused.
     *
     * @given
     */
    public void togglePaused() {
        this.paused.setValue(!this.paused.getValue());
        if (this.paused.get()) {
            this.pausedStatusText.setValue(" (Paused)");
            this.pauseMenuText.setValue("Un_pause");
        } else {
            this.pausedStatusText.setValue("");
            this.pauseMenuText.setValue("_Pause");
        }
    }

    /**
     * Saves the current state of the control tower simulation to the same files it was loaded
     * from when the application was launched.
     *
     * @throws IOException if an IOException occurs when writing to the files
     * @given
     */
    public void save() throws IOException {
        saveAs(new FileWriter(this.defaultTickSaveLocation),
                new FileWriter(this.defaultAircraftSaveLocation),
                new FileWriter(this.defaultQueuesSaveLocation),
                new FileWriter((this.defaultTerminalsSaveLocation)));
    }

    /**
     * Returns whether or not the state of the model has changed since it was last checked for a
     * change.
     *
     * @return has the model changed since last check
     * @given
     */
    public boolean isChanged() {
        return changed.get();
    }

    /**
     * Acknowledges the model has changed, and sets the changed status to false.
     *
     * @given
     */
    public void notChanged() {
        changed.setValue(false);
    }

    /**
     * Registers that the model has changed, and the view needs to be updated.
     *
     * @given
     */
    public void registerChange() {
        changed.setValue(true);
    }

    /**
     * Returns the property storing whether the simulation is paused.
     *
     * @return paused property
     * @given
     */
    public BooleanProperty getPaused() {
        return paused;
    }

    /**
     * Returns the property storing the contents of the aircraft info text box.
     *
     * @return aircraft info text box
     * @given
     */
    public StringProperty getAircraftInfoText() {
        return aircraftInfoText;
    }

    /**
     * Returns the property storing the contents of the loading aircraft info text box.
     *
     * @return loading aircraft info text box
     * @given
     */
    public StringProperty getLoadingInfoText() {
        return loadingInfoText;
    }

    /**
     * Returns the property storing the text appended to window title when the simulation is paused.
     *
     * @return paused status text property
     * @given
     */
    public StringProperty getPausedStatusText() {
        return pausedStatusText;
    }

    /**
     * Returns the property storing the text shown for the pause/unpause menu item.
     *
     * @return pause/unpause menu item text property
     * @given
     */
    public StringProperty getPauseMenuText() {
        return pauseMenuText;
    }

    /**
     * Returns the property storing the number of terminals managed by the control tower.
     *
     * @return number of terminals property
     * @given
     */
    public IntegerProperty getNumTerminals() {
        return numTerminals;
    }

    /**
     * Returns the property storing the text inside the "suitable gate" label.
     *
     * @return suitable gate label text property
     * @given
     */
    public StringProperty getSuitableGateText() {
        return suitableGateText;
    }

    /**
     * Returns the property storing the currently selected aircraft; or null if no aircraft is
     * selected.
     *
     * @return currently selected aircraft property
     * @given
     */
    public ObjectProperty<Aircraft> getSelectedAircraft() {
        return selectedAircraft;
    }

    /**
     * Returns the property storing the aircraft that is currently landing.
     *
     * @return currently landing aircraft property
     * @given
     */
    public ObjectProperty<Aircraft> getAircraftLanding() {
        return aircraftLanding;
    }

    /**
     * Returns the property storing the aircraft that is currently taking off.
     *
     * @return currently taking off aircraft property
     * @given
     */
    public ObjectProperty<Aircraft> getAircraftTakingOff() {
        return aircraftTakingOff;
    }

    /**
     * Creates and shows an error dialog.
     *
     * @param headerText  text to show in the dialog header
     * @param contentText text to show in the dialog content box
     * @given
     */
    public void createErrorDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    /**
     * Creates and shows a success dialog.
     *
     * @param headerText  text to show in the dialog header
     * @param contentText text to show in the dialog content box
     * @given
     */
    public void createSuccessDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }
}
