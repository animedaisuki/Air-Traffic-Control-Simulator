package towersim;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import towersim.display.View;
import towersim.display.ViewModel;
import towersim.util.MalformedSaveException;

import java.io.IOException;
import java.util.List;

/**
 * Entry point for the GUI of the Control Tower Simulation.
 * @given
 */
public class Launcher extends Application {

    /**
     * <b>Note</b>: you do not need to write this constructor, it is generated automatically and
     * cannot be removed from the Javadoc.
     */
    public Launcher() {}

    /**
     * Launches the GUI.
     * <p>
     * Usage: {@code tick_file aircraft_file queues_file terminalsWithGates_file}
     * <p>
     * Where
     * <ul>
     * <li>{@code tick_file} is the path to the file containing the number of ticks elapsed</li>
     * <li>{@code aircraft_file} is the path to the file containing all the aircraft managed by
     * the control tower</li>
     * <li>{@code queues_file} is the path to the file containing the takeoff and landing queues,
     * and list of loading aircraft</li>
     * <li>{@code terminalsWithGates_file} is the path to the file containing the terminals and
     * their gates</li>
     * </ul>
     *
     * @param args command line arguments
     * @given
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: tick_file aircraft_file queues_file"
                    + " terminalsWithGates_file\n");
            System.err.println("You did not specify the names of the four required save files"
                    + " from which to load.");
            System.err.println("To do this, you need to add four command line arguments to your "
                    + "program in IntelliJ.");
            System.err.println("Go to \"Run > Edit Configurations > Launcher > Program Arguments\" "
                    + "and add the paths to your files to the text box.\n");
            System.err.println("Example: saves/tick_default.txt saves/aircraft_default.txt"
                    + " saves/queues_default.txt saves/terminalsWithGates_default.txt");
            System.exit(1);
            System.out.println(args);
        }
        Application.launch(Launcher.class, args);
    }

    /**
     * {@inheritDoc}
     * @given
     */
    @Override
    public void start(Stage stage) {
        List<String> params = getParameters().getRaw();

        View view;
        try {
            view = new View(stage, new ViewModel(params));
        } catch (MalformedSaveException | IOException e) {
            System.err.println("Error loading from file. Stack trace below:");
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return;
        }

        view.run();
    }
}
