package towersim.util;

/**
 * Exception thrown when there is no suitable gate available for an aircraft.
 * @ass1
 */
public class NoSuitableGateException extends Exception {

    /**
     * Constructs a NoSuitableGateException with no detail message.
     *
     * @see Exception#Exception()
     * @ass1
     */
    public NoSuitableGateException() {
        super();
    }

    /**
     * Constructs a NoSuitableGateException that contains a helpful detail message explaining why
     * the exception occurred.
     * <p>
     * <b>Important:</b> do not write JUnit tests that expect a valid implementation of the
     * assignment to have a certain error message, as the official solution will use different
     * messages to those you are expecting, if any at all.
     *
     * @param message detail message
     * @see Exception#Exception(String)
     * @ass1
     */
    public NoSuitableGateException(String message) {
        super(message);
    }
}
