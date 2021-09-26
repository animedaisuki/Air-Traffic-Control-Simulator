package towersim.util;

/**
 * Denotes an entity whose behaviour and operations can be affected by emergencies.
 * <p>
 * Examples of emergencies in the system include mechanical or software failures, extreme weather
 * events and terrorist incidents.
 * <p>
 * Implementing classes can be in a state of emergency which may be manually cleared by calling
 * {@code clearEmergency()}.
 * @ass1
 */
public interface EmergencyState {
    /**
     * Declares a state of emergency.
     * @ass1
     */
    void declareEmergency();

    /**
     * Clears any active state of emergency.
     * <p>
     * Has no effect if there was no emergency prior to calling this method.
     * @ass1
     */
    void clearEmergency();

    /**
     * Returns whether or not a state of emergency is currently active.
     *
     * @return true if in emergency; false otherwise
     * @ass1
     */
    boolean hasEmergency();
}
