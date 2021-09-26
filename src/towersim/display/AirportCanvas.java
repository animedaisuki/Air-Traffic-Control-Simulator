package towersim.display;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import towersim.aircraft.Aircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.control.AircraftQueue;
import towersim.control.TakeoffQueue;
import towersim.ground.Gate;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Subclass of the JavaFX Canvas to represent the main elements of the airport graphically.
 * @given
 */
public class AirportCanvas extends Canvas {

    /** View model containing the main model of the application */
    private final ViewModel viewModel;

    /** Mapping of clickable regions (rectangles) to aircraft drawn on the canvas */
    private final Map<ClickableRegion, Aircraft> drawnAircraft;

    /** Width of an aircraft when drawn on the canvas, in pixels */
    private static final double AIRCRAFT_WIDTH = 75;

    /** X coordinate of the top-left corner of the runway */
    private final double runwayStartX;

    /** Horizontal width of the runway */
    private final double runwayWidth;

    /** Height of an aircraft when drawn on the canvas, in pixels */
    private static final double AIRCRAFT_HEIGHT = AIRCRAFT_WIDTH;

    /** X coordinate of the aircraft being animated on the runway */
    private final DoubleProperty runwayAnimationX = new SimpleDoubleProperty(0);

    /** Animation timeline of a landing aircraft */
    private final Timeline landTimeline;

    /** Animation timeline of an aircraft taking off */
    private final Timeline takeoffTimeline;

    /** A class to represent a rectangular region on the canvas that responds to click events */
    private static class ClickableRegion {

        /** X-coordinate of the region (top left) */
        private final double xcoord;
        /** Y-coordinate of the region (top left) */
        private final double ycoord;
        /** Width of the region, in pixels */
        private final double width;
        /** Height of the region, in pixels */
        private final double height;

        /** Creates a new clickable region with the given coordinates and dimensions */
        public ClickableRegion(double x, double y, double width, double height) {
            this.xcoord = x;
            this.ycoord = y;
            this.width = width;
            this.height = height;
        }

        /**
         * Returns whether or not the given click event's coordinates fall within this clickable
         * region
         */
        public boolean wasClicked(double clickX, double clickY) {
            return clickX >= this.xcoord && clickX <= this.xcoord + this.width
                    && clickY >= this.ycoord && clickY <= this.ycoord + this.height;
        }
    }

    /**
     * Creates a new AirportCanvas with the given dimensions.
     *
     * @param viewModel view model to use to render elements on the canvas
     * @param width width of the canvas, in pixels
     * @param height height of the canvas, in pixels
     * @given
     */
    public AirportCanvas(ViewModel viewModel, double width, double height) {
        super(width, height);

        this.viewModel = viewModel;
        this.drawnAircraft = new HashMap<>();

        this.runwayStartX = getWidth() / 2 + AIRCRAFT_WIDTH + 5;
        this.runwayWidth = getWidth() / 2 - 2 * 5 - AIRCRAFT_WIDTH;

        setOnMouseClicked(event -> {
            /* Discard any click that is not a primary (left mouse button) click */
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            double x = event.getX();
            double y = event.getY();
            Aircraft clickedAircraft = null;
            for (Map.Entry<ClickableRegion, Aircraft> entry : drawnAircraft.entrySet()) {
                if (entry.getKey().wasClicked(x, y)) {
                    clickedAircraft = entry.getValue();
                }
            }
            viewModel.getSelectedAircraft().set(clickedAircraft);
            viewModel.registerChange();


            /* Ensures the canvas gains focus when it is clicked */
            addEventFilter(MouseEvent.MOUSE_PRESSED, e -> requestFocus());
        });

        landTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(runwayAnimationX,
                                runwayStartX + runwayWidth - AIRCRAFT_WIDTH)
                ),
                new KeyFrame(Duration.seconds(1),
                        "end animation",
                        new KeyValue(runwayAnimationX, runwayStartX, Interpolator.EASE_OUT)
                )
        );

        takeoffTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(runwayAnimationX,
                                runwayStartX + runwayWidth - AIRCRAFT_WIDTH - 100)
                ),
                new KeyFrame(Duration.seconds(1),
                        "end animation",
                        e -> drawRunway(),
                        new KeyValue(runwayAnimationX, runwayStartX - AIRCRAFT_WIDTH,
                                Interpolator.EASE_IN)
                )
        );
    }

    /**
     * Draws all the relevant elements of the airport onto the canvas.
     *
     * @given
     */
    public void draw() {
        this.drawnAircraft.clear();

        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, getWidth(), getHeight());

        drawRunway();
        drawQueue(viewModel.getControlTower().getTakeoffQueue(), 0, 0);
        drawQueue(viewModel.getControlTower().getLandingQueue(), 0, AIRCRAFT_HEIGHT);
        drawAwayAircraft();
        drawTerminals();
        drawTickStatus();
    }

    /* Draws the runway */
    private void drawRunway() {
        GraphicsContext gc = getGraphicsContext2D();

        final double runwayHeight = AIRCRAFT_HEIGHT;
        final double marginTop = 5;
        final double lineLength = 30;
        final double runwayTarmacWidth = AIRCRAFT_WIDTH;

        gc.setFill(Color.gray(0.2));
        gc.fillRect(runwayStartX,
                AIRCRAFT_HEIGHT + marginTop,
                runwayTarmacWidth,
                runwayHeight);
        gc.setFill(Color.BLACK);
        gc.fillRect(runwayStartX + runwayTarmacWidth,
                AIRCRAFT_HEIGHT + marginTop,
                runwayWidth - runwayTarmacWidth,
                runwayHeight);

        for (int i = 0; i < ((runwayWidth - runwayTarmacWidth) - lineLength) / lineLength; ++i) {
            gc.setStroke(Color.WHITE);
            final double lineY = AIRCRAFT_HEIGHT + marginTop + (runwayHeight / 2);
            final double lineStartOffset = 7; // makes lines look more centered
            gc.strokeLine(runwayStartX + runwayTarmacWidth + lineStartOffset + lineLength / 2
                            + (i * lineLength),
                    lineY,
                    runwayStartX + runwayTarmacWidth + lineStartOffset + lineLength
                            + (i * lineLength),
                    lineY);
        }
    }

    /**
     * Performs the animation of the aircraft currently landing or taking off.
     * <p>
     * Called once per tick of the view model.
     *
     * @given
     */
    public void animate() {
        Aircraft aircraftToAnimate;
        boolean takingOff = viewModel.getAircraftTakingOff().isNotNull().get();
        boolean landing = viewModel.getAircraftLanding().isNotNull().get();
        if (takingOff) {
            aircraftToAnimate = viewModel.getAircraftTakingOff().get();
        } else if (landing) {
            aircraftToAnimate = viewModel.getAircraftLanding().get();
        } else {
            return;
        }

        Aircraft finalAircraftToAnimate = aircraftToAnimate;
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();

                drawAircraft(finalAircraftToAnimate,
                        runwayAnimationX.doubleValue(),
                        AIRCRAFT_HEIGHT + 5,
                        Color.WHITE);
            }
        };
        timer.start();
        if (takingOff) {
            takeoffTimeline.play();
        } else {
            landTimeline.play();
        }
    }

    /* Draws an aircraft queue */
    private void drawQueue(AircraftQueue queue, double x, double y) {
        GraphicsContext gc = getGraphicsContext2D();

        final int queueCapacity = 6;
        final double labelWidth = 65;

        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, AIRCRAFT_WIDTH * queueCapacity + labelWidth, AIRCRAFT_HEIGHT);

        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, AIRCRAFT_WIDTH * queueCapacity + labelWidth, AIRCRAFT_HEIGHT);

        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("monospace", FontWeight.BOLD, 30));
        String labelText;
        if (queue instanceof TakeoffQueue) {
            labelText = "T/O";
        } else {
            labelText = "LND";
        }
        gc.fillText(labelText, x + 5, y + AIRCRAFT_HEIGHT / 2);

        gc.setStroke(Color.BLACK);
        gc.strokeLine(x + labelWidth, y, x + labelWidth, y + AIRCRAFT_HEIGHT);

        // Draw aircraft in queue
        var aircraft = queue.getAircraftInOrder();

        for (int i = 0; i < aircraft.size(); ++i) {
            Aircraft a = aircraft.get(i);
            drawAircraft(a, x + labelWidth + AIRCRAFT_WIDTH * i, y, Color.BLACK);
        }
    }

    /* Draws the list of aircraft that are currently AWAY */
    private void drawAwayAircraft() {
        GraphicsContext gc = getGraphicsContext2D();

        final int capacity = 6;
        final double labelWidth = 85;
        final double x = getWidth() / 2 + 5;
        final double y = 0;

        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, AIRCRAFT_WIDTH * capacity + labelWidth, AIRCRAFT_HEIGHT);

        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, AIRCRAFT_WIDTH * capacity + labelWidth, AIRCRAFT_HEIGHT);

        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("monospace", FontWeight.BOLD, 30));
        gc.fillText("AWAY", x + 5, y + AIRCRAFT_HEIGHT / 2);

        gc.setStroke(Color.BLACK);
        gc.strokeLine(x + labelWidth, y, x + labelWidth, y + AIRCRAFT_HEIGHT);

        // Draw aircraft in queue
        var aircraft = viewModel.getControlTower().getAircraft().stream()
                .filter(a -> a.getTaskList().getCurrentTask().getType() == TaskType.AWAY)
                .collect(Collectors.toList());

        for (int i = 0; i < aircraft.size(); ++i) {
            Aircraft a = aircraft.get(i);
            drawAircraft(a, x + labelWidth + AIRCRAFT_WIDTH * i, y, Color.BLACK);
        }
    }

    /* Draws the terminals and their gates */
    private void drawTerminals() {
        GraphicsContext gc = getGraphicsContext2D();

        final double terminalLabelHeight = 25;
        final double terminalAircraftHeight = AIRCRAFT_HEIGHT;
        final double terminalHeight = terminalLabelHeight + terminalAircraftHeight;
        final double marginBelow = 5;
        final double marginLeft = 5;
        final double spaceAbove = 2 * AIRCRAFT_HEIGHT + 2 * marginBelow; // queues + padding
        final double terminalWidth = getWidth() / 2 - (2 * marginLeft);

        List<Terminal> terminals = this.viewModel.getControlTower().getTerminals();

        for (int i = 0; i < terminals.size(); ++i) {
            Terminal terminal = terminals.get(i);

            final double terminalStartX = marginLeft + (i % 2 == 1
                    ? terminalWidth + 2 * marginLeft
                    : 0);
            final double terminalStartY = spaceAbove + marginBelow
                    + ((i / 2) * (terminalHeight + marginBelow));

            gc.setFill(Color.gray(0.7));
            gc.fillRect(terminalStartX,
                    terminalStartY,
                    terminalWidth,
                    terminalLabelHeight);

            if (terminal.hasEmergency()) {
                gc.setFill(Color.RED);
            } else {
                gc.setFill(Color.BLACK);
            }
            gc.setTextBaseline(VPos.CENTER);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("sans-serif", FontWeight.BOLD, 14));

            String terminalText = terminal.getClass().getSimpleName() + " "
                    + terminal.getTerminalNumber();
            if (terminal.hasEmergency()) {
                terminalText += " (emergency)";
            }
            gc.fillText(terminalText,
                    terminalStartX + terminalWidth / 2,
                    terminalStartY + 0.5 * terminalLabelHeight);

            // Number of gates and max number of gates
            String numGatesText = terminal.getGates().size() + "/" + Terminal.MAX_NUM_GATES
                    + " gates";
            gc.setFill(Color.BLACK);
            gc.setTextBaseline(VPos.CENTER);
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setFont(Font.font("sans-serif", FontWeight.NORMAL, 14));
            gc.fillText(numGatesText,
                    terminalStartX + 2, // 2px left padding
                    terminalStartY + 0.5 * terminalLabelHeight);

            // Occupancy level
            String occupancyText = terminal.calculateOccupancyLevel() + "%";
            gc.setFill(Color.BLACK);
            gc.setTextBaseline(VPos.CENTER);
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setFont(Font.font("sans-serif", FontWeight.NORMAL, 14));
            gc.fillText(occupancyText,
                    terminalStartX + terminalWidth - 2, // 2px right padding
                    terminalStartY + 0.5 * terminalLabelHeight);

            gc.setFill(Color.gray(0.2));
            gc.fillRect(terminalStartX,
                    terminalStartY + terminalLabelHeight,
                    terminalWidth,
                    terminalAircraftHeight);

            List<Gate> gates = terminal.getGates();
            for (int j = 0; j < gates.size(); ++j) {
                Gate gate = gates.get(j);

                final double gateWidth = AIRCRAFT_WIDTH + 15;

                // Draw gate number
                gc.setFill(Color.WHITE);
                gc.setTextBaseline(VPos.CENTER);
                gc.setTextAlign(TextAlignment.LEFT);
                gc.setFont(Font.font("monospace", FontWeight.BOLD, 12));
                gc.fillText(String.valueOf(gate.getGateNumber()),
                        terminalStartX + 2 + gateWidth * j, // 2px left padding
                        terminalStartY + terminalLabelHeight + terminalAircraftHeight / 2.0);

                // Draw dividing line
                final double gateLineX = terminalStartX + gateWidth * (j + 1);
                if (j != Terminal.MAX_NUM_GATES - 1) {
                    gc.setStroke(Color.WHITE);
                    gc.strokeLine(gateLineX,
                            terminalStartY + terminalLabelHeight,
                            gateLineX,
                            terminalStartY + terminalLabelHeight + terminalAircraftHeight);
                }

                // Draw parked aircraft
                if (gate.isOccupied()) {
                    drawAircraft(gate.getAircraftAtGate(),
                            gateLineX - AIRCRAFT_WIDTH,
                            terminalStartY + terminalLabelHeight,
                            Color.WHITE);
                }
            }
        }
    }

    /*
     * Draws an aircraft at the given position on the canvas.
     *
     * @param aircraft aircraft to draw
     * @param x x-coord of top left corner
     * @param y y-coord of top left corner
     * @param textColor color to use when drawing aircraft info text
     */
    private void drawAircraft(Aircraft aircraft, double x, double y, Color textColor) {
        GraphicsContext gc = getGraphicsContext2D();

        this.drawnAircraft.put(new ClickableRegion(x, y, AIRCRAFT_WIDTH, AIRCRAFT_HEIGHT),
                aircraft);

        if (aircraft instanceof PassengerAircraft) {
            gc.setFill(Color.CADETBLUE);
        } else {
            gc.setFill(Color.SADDLEBROWN);
        }

        // Emergency
        if (aircraft.hasEmergency()) {
            textColor = Color.RED;
        }

        // Is selected
        FontWeight fontWeight = FontWeight.NORMAL;
        if (Objects.equals(aircraft, viewModel.getSelectedAircraft().get())) {
            fontWeight = FontWeight.BOLD;
        }

        switch (aircraft.getCharacteristics().type) {
            case HELICOPTER:
                drawHelicopter(x, y);
                break;
            case AIRPLANE:
            default:
                drawAirplane(x, y);
        }

        // Text
        gc.setFill(textColor);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("monospace", fontWeight, 12));

        Task currentTask = aircraft.getTaskList().getCurrentTask();
        String aircraftTaskLine;
        if (currentTask.getType() == TaskType.LOAD) {
            aircraftTaskLine = "LOAD@" + currentTask.getLoadPercent() + "%";
        } else {
            aircraftTaskLine = currentTask.getType().name();
        }
        String aircraftText = aircraft.getCallsign() + System.lineSeparator()
                + aircraftTaskLine + System.lineSeparator()
                + aircraft.calculateOccupancyLevel() + "%";
        gc.fillText(aircraftText,
                x + AIRCRAFT_WIDTH / 2,
                y + AIRCRAFT_HEIGHT);
    }

    private void drawAirplane(double x, double y) {
        GraphicsContext gc = getGraphicsContext2D();

        // Wings
        gc.fillPolygon(new double[] {
            x + AIRCRAFT_WIDTH / 2 + 8,
            x + AIRCRAFT_WIDTH / 2 + 8,
            x + AIRCRAFT_WIDTH / 2 - 10
        }, new double[] {
            y + 4,
            y + 36,
            y + 22}, 3);

        // Tail
        gc.fillPolygon(new double[] {
            x + AIRCRAFT_WIDTH - 4,
            x + AIRCRAFT_WIDTH - 16,
            x + AIRCRAFT_WIDTH - 4
        }, new double[] {
            y + 22,
            y + 18,
            y + 4}, 3);

        // Fuselage
        gc.fillRoundRect(x + 4,
                y + 17,
                AIRCRAFT_WIDTH - 8,
                8,
                10,
                10);
    }

    private void drawHelicopter(double x, double y) {
        GraphicsContext gc = getGraphicsContext2D();


        // Fuselage
        gc.fillOval(x + 15, y + 16, 30, 16);

        // Main rotor
        gc.fillRect(x + 30 - 1, y + 16 - 4, 3, 4);
        gc.fillRect(x + 4, y + 10, 50, 3);

        // Tail rotor
        gc.fillRect(x + 30, y + 22, 40, 3);
        gc.fillRect(x + AIRCRAFT_WIDTH - 14, y + 16, 2, 14);
    }

    /* Draws the status bar containing tick information */
    private void drawTickStatus() {
        GraphicsContext gc = getGraphicsContext2D();

        final double height = 20;

        gc.setFill(Color.gray(0.5));
        gc.fillRect(0, getHeight() - height, getWidth(), height);

        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("sans-serif", 14));
        gc.fillText(String.format("%d ticks elapsed",
                viewModel.getControlTower().getTicksElapsed()),
                getWidth() / 2, getHeight() - 0.5 * height);
    }
}
