package towersim.util;

/**
 * Exception thrown when there is no suitable gate available for an aircraft.
 */
public class NoSuitableGateException extends Exception {

    /**
     * Constructs a NoSuitableGateException with no detail message.
     *
     * @see Exception#Exception()
     */
    public NoSuitableGateException() {
        super();
    }

    /**
     * Constructs a NoSuitableGateException that contains a helpful detail message explaining why
     * the exception occurred.
     * @param message detail message
     */
    public NoSuitableGateException(String message) {
        super(message);
    }
}
