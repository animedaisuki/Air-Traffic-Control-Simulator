package towersim.display;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * View for the Control Tower Simulation GUI.
 * @given
 */
public class View {
    /** Stage containing the application scene */
    private final Stage stage;

    /** ViewModel that manages interaction with the model */
    private final ViewModel viewModel;

    /** Custom canvas that represents the state of the simulation graphically */
    private AirportCanvas canvas;

    /** Last recorded time in nanoseconds */
    private long lastNanoTime;

    /** Time spent un-paused since last tick, in nanoseconds */
    private long timeSpentUnpaused = 0;

    /** Time interval between ticks of the view model */
    private final IntegerProperty secondsPerTick = new SimpleIntegerProperty(5);

    /** Maximum number of terminals that can be displayed */
    private static final int MAX_TERMINALS = 6;

    /**
     * Creates a new view for the given view model and adds the associated GUI elements to the given
     * stage.
     *
     * @param stage stage to add GUI elements to
     * @param viewModel view model to display
     * @given
     */
    public View(Stage stage, ViewModel viewModel) {
        this.stage = stage;
        this.viewModel = viewModel;

        stage.setResizable(false);

        stage.titleProperty().bind(Bindings.concat("Control Tower Simulation",
                viewModel.getPausedStatusText()));

        Scene rootScene = new Scene(createWindow());
        stage.setScene(rootScene);
    }

    /* Creates the root window containing all GUI elements */
    private Pane createWindow() {
        this.canvas = new AirportCanvas(viewModel, 1100, 500);
        BorderPane.setAlignment(canvas, Pos.TOP_CENTER);

        var space = new Region();
        VBox.setVgrow(space, Priority.ALWAYS);

        var buttons = new HBox();
        buttons.setPadding(new Insets(10, 10, 10, 10));
        buttons.setSpacing(10);
        var droneAlertButton = new Button("Drone Alert");
        droneAlertButton.setOnAction(viewModel.getDroneAlertHandler());
        var droneClearButton = new Button("Clear Drone Alert");
        droneClearButton.setOnAction(viewModel.getDroneClearHandler());
        var findSuitableGateButton = new Button("Find Gate for Selected Aircraft");
        findSuitableGateButton.setOnAction(viewModel.getFindSuitableGateHandler());
        buttons.getChildren().add(droneAlertButton);
        buttons.getChildren().add(droneClearButton);
        buttons.getChildren().add(findSuitableGateButton);
        var gateInfoLabel = new Label();
        gateInfoLabel.textProperty().bind(viewModel.getSuitableGateText());
        buttons.getChildren().add(gateInfoLabel);

        var bottomRightPanel = new VBox();
        bottomRightPanel.getChildren().add(buttons);
        bottomRightPanel.getChildren().add(space);
        var rightInfoBox = createInfoBox(viewModel.getLoadingInfoText(), 6);
        bottomRightPanel.getChildren().add(rightInfoBox);

        var bottomPanel = new HBox();
        var leftInfoBox = createInfoBox(viewModel.getAircraftInfoText(), 9);
        bottomPanel.getChildren().add(leftInfoBox);
        bottomPanel.getChildren().add(bottomRightPanel);

        var pane = new VBox();
        pane.getChildren().add(createMenuBar());
        pane.getChildren().add(canvas);
        pane.getChildren().add(bottomPanel);
        return pane;
    }

    /* Creates a menu bar that allows actions to be taken within the GUI */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        MenuItem save = new MenuItem("_Save");
        save.setMnemonicParsing(true);
        save.setOnAction(event -> {
            try {
                viewModel.save();
            } catch (IOException e) {
                viewModel.createErrorDialog("Error saving to file",
                        e.getMessage());
                return;
            }
            viewModel.createSuccessDialog("Saved successfully",
                    "Saved to default provided file locations successfully.");
        });

        MenuItem exit = new MenuItem("_Exit");
        exit.setMnemonicParsing(true);
        exit.setOnAction(event -> System.exit(0));
        exit.setAccelerator(KeyCombination.keyCombination("Shortcut+Q"));

        Menu menuFile = new Menu("_File");
        menuFile.setMnemonicParsing(true);
        menuFile.getItems().add(save);
        menuFile.getItems().add(createSaveAsMenuItem());
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().add(exit);

        Menu add = new Menu("_Add");
        add.setMnemonicParsing(true);
        add.getItems().add(createAddTerminalMenu());
        add.getItems().add(createAddGateMenu());
        add.getItems().add(createAddAircraftMenu());

        MenuItem emergencyAircraft = new MenuItem("On selected _aircraft...");
        emergencyAircraft.setMnemonicParsing(true);
        emergencyAircraft.disableProperty().bind(viewModel.getSelectedAircraft().isNull());
        emergencyAircraft.setOnAction(e -> {
            var selectedAircraft = viewModel.getSelectedAircraft().get();
            if (selectedAircraft.hasEmergency()) {
                selectedAircraft.clearEmergency();
            } else {
                selectedAircraft.declareEmergency();
            }
            viewModel.registerChange();
        });
        MenuItem emergencyTerminal = new MenuItem("On a _terminal...");
        emergencyTerminal.setMnemonicParsing(true);
        emergencyTerminal.setOnAction(e -> {
            var choice = chooseTerminal("Toggle Emergency",
                    "Please choose a terminal to toggle emergency on");
            if (choice.isEmpty()) {
                return;
            }
            var terminal = choice.get();
            if (terminal.hasEmergency()) {
                terminal.clearEmergency();
            } else {
                terminal.declareEmergency();
            }
            viewModel.registerChange();
        });
        Menu emergency = new Menu("Toggle _emergency");
        emergency.setMnemonicParsing(true);
        emergency.getItems().add(emergencyAircraft);
        emergency.getItems().add(emergencyTerminal);
        Menu menuActions = new Menu("_Actions");
        menuActions.setMnemonicParsing(true);
        menuActions.getItems().add(add);
        menuActions.getItems().add(emergency);

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(createSimMenu());
        menuBar.getMenus().add(menuActions);
        return menuBar;
    }

    /* Creates a menu containing actions related to controlling the simulation */
    private Menu createSimMenu() {
        MenuItem pause = new MenuItem();
        pause.setMnemonicParsing(true);
        pause.textProperty().bind(viewModel.getPauseMenuText());
        pause.setOnAction(event -> viewModel.togglePaused());
        pause.setAccelerator(KeyCombination.keyCombination("Shortcut+P"));
        MenuItem lowSpeed = new MenuItem("_5 seconds per tick");
        lowSpeed.setMnemonicParsing(true);
        lowSpeed.setOnAction(e -> secondsPerTick.set(5));
        lowSpeed.disableProperty().bind(secondsPerTick.isEqualTo(5));
        MenuItem medSpeed = new MenuItem("_3 seconds per tick");
        medSpeed.setMnemonicParsing(true);
        medSpeed.setOnAction(e -> secondsPerTick.set(3));
        medSpeed.disableProperty().bind(secondsPerTick.isEqualTo(3));
        MenuItem highSpeed = new MenuItem("_1 second per tick");
        highSpeed.setMnemonicParsing(true);
        highSpeed.setOnAction(e -> secondsPerTick.set(1));
        highSpeed.disableProperty().bind(secondsPerTick.isEqualTo(1));
        Menu menuSim = new Menu("_Simulation");
        menuSim.setMnemonicParsing(true);
        Menu speed = new Menu("_Speed");
        speed.setMnemonicParsing(true);
        speed.getItems().add(lowSpeed);
        speed.getItems().add(medSpeed);
        speed.getItems().add(highSpeed);
        menuSim.getItems().add(pause);
        menuSim.getItems().add(speed);
        return menuSim;
    }

    /* Creates a menu item that, when clicked, prompts for a new terminal to be added */
    private MenuItem createAddTerminalMenu() {
        MenuItem addTerminal = new MenuItem("New _terminal...");
        addTerminal.setMnemonicParsing(true);
        addTerminal.setOnAction(event -> {
            var defaultTerminalNumber = 1;
            var highestTerminalNumber = viewModel.getControlTower().getTerminals()
                    .stream()
                    .mapToInt(Terminal::getTerminalNumber)
                    .max();
            if (highestTerminalNumber.isPresent()) {
                defaultTerminalNumber = highestTerminalNumber.getAsInt() + 1;
            }
            var terminalNumber = getResponse("Add Terminal",
                    "Please enter the terminal number",
                    "Terminal number:",
                    defaultTerminalNumber);
            if (terminalNumber.isEmpty() || terminalNumber.get() < 1) {
                return;
            }
            /* Can't create a new terminal with the same number as an existing one */
            if (viewModel.getControlTower().getTerminals().stream()
                    .anyMatch(t -> t.getTerminalNumber() == terminalNumber.get())) {
                viewModel.createErrorDialog("Cannot create terminal",
                        "Terminal with number " + terminalNumber.get() + " already exists");
                return;
            }
            var validTerminalTypes = List.of("AirplaneTerminal", "HelicopterTerminal");
            var terminalType = getChoice("Add Terminal",
                    "Please choose the terminal's type", "Terminal type:",
                    validTerminalTypes.get(0),
                    validTerminalTypes.toArray(new String[0]));
            if (terminalType.isEmpty()) {
                return;
            }
            Terminal newTerminal;
            if (terminalType.get().equals("AirplaneTerminal")) {
                newTerminal = new AirplaneTerminal(terminalNumber.get());
            } else {
                newTerminal = new HelicopterTerminal(terminalNumber.get());
            }
            viewModel.getControlTower().addTerminal(newTerminal);
            viewModel.getNumTerminals().set(viewModel.getNumTerminals().get() + 1);
            viewModel.registerChange();
        });
        addTerminal.disableProperty().bind(Bindings.greaterThan(viewModel.getNumTerminals(),
                MAX_TERMINALS - 1));
        return addTerminal;
    }

    /* Creates a menu item that, when clicked, prompts for a new gate to be added */
    private MenuItem createAddGateMenu() {
        MenuItem addGate = new MenuItem("New _gate...");
        addGate.setMnemonicParsing(true);
        addGate.setOnAction(e -> {
            var choice = chooseTerminal("Add Gate to Terminal",
                    "Please choose a terminal to add a gate to");
            if (choice.isEmpty()) {
                return;
            }
            var terminal = choice.get();
            if (terminal.getGates().size() == Terminal.MAX_NUM_GATES) {
                viewModel.createErrorDialog("Cannot create gate", "Terminal "
                        + terminal.getTerminalNumber()
                        + " already has the maximum possible number of gates.");
                return;
            }
            var defaultGateNumber = 1;
            List<Gate> allGates = new ArrayList<>();
            for (Terminal t : viewModel.getControlTower().getTerminals()) {
                allGates.addAll(t.getGates());
            }
            var highestGateNumber = allGates
                    .stream()
                    .mapToInt(Gate::getGateNumber)
                    .max();
            if (highestGateNumber.isPresent()) {
                defaultGateNumber = highestGateNumber.getAsInt() + 1;
            }
            var gateNumberChoice = getResponse("Add Gate to Terminal",
                    "Please choose a gate number for the new gate", "Gate number:",
                    defaultGateNumber);
            if (gateNumberChoice.isEmpty() || gateNumberChoice.get() < 1) {
                return;
            }
            /* Gate number must be unique */
            if (allGates.stream().anyMatch(g -> g.getGateNumber() == gateNumberChoice.get())) {
                viewModel.createErrorDialog("Cannot create gate",
                        "A gate already exists with number " + gateNumberChoice.get());
                return;
            }
            try {
                terminal.addGate(new Gate(gateNumberChoice.get()));
            } catch (NoSpaceException ex) {
                // ignored (not possible)
            }
            viewModel.registerChange();
        });
        return addGate;
    }

    /* Creates a menu item that, when clicked, prompts for a new aircraft to be added */
    private MenuItem createAddAircraftMenu() {
        MenuItem addAircraft = new MenuItem("New _aircraft...");
        addAircraft.setMnemonicParsing(true);
        addAircraft.setOnAction(event -> {
            Random random = new Random();
            var taskList1 = new TaskList(List.of(new Task(TaskType.WAIT),
                    new Task(TaskType.LOAD, 90),
                    new Task(TaskType.TAKEOFF),
                    new Task(TaskType.AWAY),
                    new Task(TaskType.AWAY),
                    new Task(TaskType.AWAY),
                    new Task(TaskType.LAND)));
            var taskList2 = new TaskList(List.of(new Task(TaskType.WAIT),
                    new Task(TaskType.WAIT),
                    new Task(TaskType.LOAD, 75),
                    new Task(TaskType.TAKEOFF),
                    new Task(TaskType.AWAY),
                    new Task(TaskType.AWAY),
                    new Task(TaskType.AWAY),
                    new Task(TaskType.AWAY),
                    new Task(TaskType.LAND)));
            var aircraftPresets = new TreeMap<>(Map.of(
                    "Passenger Airplane (BOEING_787)",
                    new PassengerAircraft(generateRandomCallsign(
                            new String[] {"QFA", "CSN", "UAL", "UAE"}[random.nextInt(4)],
                            viewModel.getControlTower().getAircraft()),
                            AircraftCharacteristics.BOEING_787,
                            taskList1,
                            AircraftCharacteristics.BOEING_787.fuelCapacity / 6, 0),
                    "Freight Airplane (BOEING_747_8F)",
                    new FreightAircraft(generateRandomCallsign(
                            new String[] {"UPS", "GTI", "CLX", "GEC"}[random.nextInt(4)],
                            viewModel.getControlTower().getAircraft()),
                            AircraftCharacteristics.BOEING_747_8F,
                            taskList2,
                            AircraftCharacteristics.BOEING_747_8F.fuelCapacity / 8, 0),
                    "Passenger Helicopter (ROBINSON_R44)",
                    new PassengerAircraft(generateRandomCallsign(null,
                            viewModel.getControlTower().getAircraft()),
                            AircraftCharacteristics.ROBINSON_R44,
                            taskList2,
                            AircraftCharacteristics.ROBINSON_R44.fuelCapacity / 4, 1),
                    "Freight Helicopter (SIKORSKY_SKYCRANE)",
                    new FreightAircraft(generateRandomCallsign(null,
                            viewModel.getControlTower().getAircraft()),
                            AircraftCharacteristics.SIKORSKY_SKYCRANE,
                            taskList1,
                            AircraftCharacteristics.SIKORSKY_SKYCRANE.fuelCapacity / 10, 0)));
            var choice = getChoice("Add Aircraft",
                    "Please choose an aircraft from the list of preset aircraft",
                    "Aircraft",
                    aircraftPresets.keySet().toArray(new String[0])[0],
                    aircraftPresets.keySet().toArray(new String[0]));
            if (choice.isEmpty()) {
                return;
            }
            String chosenKey = choice.get();
            Aircraft chosenAircraft = aircraftPresets.get(chosenKey);
            try {
                viewModel.getControlTower().addAircraft(chosenAircraft);
            } catch (NoSuitableGateException e) {
                viewModel.createErrorDialog("Cannot create aircraft",
                        "No suitable gate for aircraft " + chosenAircraft);
                return;
            }
            viewModel.registerChange();
            viewModel.createSuccessDialog("Successfully created aircraft",
                    "Aircraft created:\n" + chosenAircraft);
        });
        return addAircraft;
    }

    /* Creates a menu item that, when clicked, prompts for the state of the model to be saved */
    private MenuItem createSaveAsMenuItem() {
        MenuItem saveAs = new MenuItem("Save _As...");
        saveAs.setMnemonicParsing(true);
        saveAs.setOnAction(event -> {
            List<String> filesToSave = List.of("Tick file", "Aircraft file", "Queues file",
                    "Terminals with gates file");
            List<String> enteredFilenames = new ArrayList<>(4);
            for (String fileToSave : filesToSave) {
                var filename = getResponse("Save to " + fileToSave,
                        "Please enter the path of the file to save to", fileToSave + " name", "");
                if (filename.isEmpty()) {
                    return;
                }
                enteredFilenames.add(filename.get());
            }
            try {
                viewModel.saveAs(new FileWriter(enteredFilenames.get(0)),
                        new FileWriter(enteredFilenames.get(1)),
                        new FileWriter(enteredFilenames.get(2)),
                        new FileWriter(enteredFilenames.get(3)));
            } catch (IOException e) {
                viewModel.createErrorDialog("Error saving to file",
                        e.getMessage());
                return;
            }
            viewModel.createSuccessDialog("Saved files successfully",
                    "Saved to \"" + enteredFilenames + "\" successfully.");
        });
        saveAs.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        return saveAs;
    }

    /* Generates a random callsign based on the given airline code and list of existing aircraft */
    private String generateRandomCallsign(String airlineCode, List<Aircraft> existingAircraft) {
        Random random = new Random();
        final int numDigitsInCallsign = 3;
        StringBuilder builder;
        String callsign;
        boolean isUnique;
        do {
            builder = new StringBuilder();
            for (int i = 0; i < numDigitsInCallsign; i++) {
                if (airlineCode == null) {
                    // Random letter
                    builder.append((char) (((int) 'A') + random.nextInt(26)));
                } else {
                    // Random digit
                    builder.append((char) (((int) '0') + random.nextInt(10)));
                }
            }
            callsign = (airlineCode != null ? airlineCode : "VH-") + builder;
            String finalCallsign = callsign;
            isUnique = existingAircraft.stream()
                    .noneMatch(a -> a.getCallsign().equals(finalCallsign));
        } while (!isUnique);

        return callsign;
    }

    /* Prompts the user to choose a terminal from a list of all the control tower's terminals */
    private Optional<Terminal> chooseTerminal(String title, String header) {
        var terminalOptions = new TreeMap<String, Terminal>();
        for (Terminal terminal : viewModel.getControlTower().getTerminals()) {
            terminalOptions.put(terminal.toString(), terminal);
        }
        var choice = getChoice(title, header, "Terminal:",
                terminalOptions.keySet().toArray(new String[0])[0],
                terminalOptions.keySet().toArray(new String[0]));
        if (choice.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(terminalOptions.get(choice.get()));
    }

    /* Creates a non-editable text area to display some text information */
    private TextArea createInfoBox(StringProperty contents, int rowCount) {
        var infoBox = new TextArea();
        infoBox.textProperty().bind(contents);
        infoBox.setEditable(false);
        infoBox.setFocusTraversable(false);
        infoBox.setWrapText(false);
        infoBox.setFont(Font.font(14));
        infoBox.setPrefRowCount(rowCount);
        return infoBox;
    }

    /***
     * Prompts the user for a textual response via a dialog box.
     *
     * @param title title of dialog box window
     * @param header header text of dialog box
     * @param label label text to display beside input box
     * @param defaultValue initial contents of the input box
     * @return value entered by the user
     * @given
     */
    public Optional<String> getResponse(String title, String header,
            String label, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(label);
        dialog.setGraphic(null);
        return dialog.showAndWait();
    }

    /***
     * Prompts the user for a numeric response via a dialog box.
     *
     * @param title title of dialog box window
     * @param header header text of dialog box
     * @param label label text to display beside input box
     * @param defaultValue initial contents of the input box
     * @return value entered by the user
     * @given
     */
    public Optional<Integer> getResponse(String title, String header,
            String label, int defaultValue) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(defaultValue));
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(label);
        dialog.setGraphic(null);
        // Only allow numeric values to be entered
        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        return dialog.showAndWait().map(Integer::valueOf);
    }

    /* Prompts the user for a choice from a list of options */
    @SafeVarargs
    private <T> Optional<T> getChoice(String title, String header, String label,
            T defaultChoice, T... choices) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(label);
        dialog.setGraphic(null);
        return dialog.showAndWait();
    }

    /**
     * Initialises the view and begins the timer responsible for performing ticks
     *
     * @given
     */
    public void run() {
        final long nanosPerSecond = 1000000000;

        new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                if (viewModel.isChanged()) {
                    viewModel.notChanged();
                    canvas.draw();
                }

                if (viewModel.getPaused().get()) {
                    lastNanoTime = currentNanoTime;
                    return;
                }

                timeSpentUnpaused += currentNanoTime - lastNanoTime;
                lastNanoTime = currentNanoTime;

                if (timeSpentUnpaused > secondsPerTick.get() * nanosPerSecond) {
                    timeSpentUnpaused = 0;
                    viewModel.tick();
                    canvas.animate();
                }
            }
        }.start();

        this.stage.show();
        this.canvas.draw();
    }
}
