package towersim.ground;

/**
 * Represents an airport terminal that is designed to accommodate helicopters.
 */
public class HelicopterTerminal extends Terminal {
    /**
     * Creates a new HelicopterTerminal with the given unique terminal number.
     * <p>
     * See {@link Terminal#Terminal(int)}.
     *
     * @param terminalNumber identifying number of this helicopter terminal
     */
    public HelicopterTerminal(int terminalNumber) {
        super(terminalNumber);
    }
}
