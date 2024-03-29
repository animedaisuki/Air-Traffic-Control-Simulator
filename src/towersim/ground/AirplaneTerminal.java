package towersim.ground;

/**
 * Represents an airport terminal that is designed to accommodate airplanes.
 */
public class AirplaneTerminal extends Terminal {
    /**
     * Creates a new AirplaneTerminal with the given unique terminal number.
     * <p>
     * See {@link Terminal#Terminal(int)}.
     *
     * @param terminalNumber identifying number of this airplane terminal
     */
    public AirplaneTerminal(int terminalNumber) {
        super(terminalNumber);
    }
}
