package towersim.aircraft;

/**
 * Stores information about particular models of aircraft.
 * <p>
 * Characteristics of an individual aircraft include the type of aircraft, its empty weight, fuel
 * capacity, etc.
 * <table border="1">
 * <caption>Enum definitions</caption>
 * <tr><th>AircraftCharacteristics</th>
 * <th>Aircraft type (AircraftType)</th>
 * <th>Empty weight (kilograms)</th>
 * <th>Fuel capacity (litres)</th>
 * <th>Passenger capacity</th>
 * <th>Freight capacity (kilograms)</th></tr>
 * <tr><td>{@code AIRBUS_A320}</td><td>{@code AIRPLANE}</td><td>42600</td><td>27200</td><td>150</td>
 * <td>0</td></tr>
 * <tr><td>{@code BOEING_747_8F}</td><td>{@code AIRPLANE}</td><td>197131</td><td>226117</td>
 * <td>0</td><td>137756</td></tr>
 * <tr><td>{@code ROBINSON_R44}</td><td>{@code HELICOPTER}</td><td>658</td><td>190</td><td>4</td>
 * <td>0</td></tr>
 * <tr><td>{@code BOEING_787}</td><td>{@code AIRPLANE}</td><td>119950</td><td>126206</td>
 * <td>242</td><td>0</td></tr>
 * <tr><td>{@code FOKKER_100}</td><td>{@code AIRPLANE}</td><td>24375</td><td>13365</td><td>97</td>
 * <td>0</td></tr>
 * <tr><td>{@code SIKORSKY_SKYCRANE}</td><td>{@code HELICOPTER}</td><td>8724</td><td>3328</td>
 * <td>0</td><td>9100</td></tr>
 * </table>
 * <p>
 * <b>NOTE:</b> You do <b>not</b> need to implement the {@code values()} or
 * {@code valueOf(String)} methods as part of the assignment. Their implementations are generated
 * automatically by the compiler.
 * @ass1
 */
public enum AircraftCharacteristics {
    /**
     * Narrow-body twin-jet airliner.
     * @ass1
     */
    AIRBUS_A320(AircraftType.AIRPLANE, 42600, 27200, 150, 0),

    /**
     * Wide-body quad-jet freighter.
     * @ass1
     */
    BOEING_747_8F(AircraftType.AIRPLANE, 197131, 226117, 0, 137756),

    /**
     * Four-seater light helicopter.
     * @ass1
     */
    ROBINSON_R44(AircraftType.HELICOPTER, 658, 190, 4, 0),

    /**
     * Long range, wide-body twin-jet airliner.
     * @ass1
     */
    BOEING_787(AircraftType.AIRPLANE, 119950, 126206, 242, 0),

    /**
     * Twin-jet regional airliner.
     * @ass1
     */
    FOKKER_100(AircraftType.AIRPLANE, 24375, 13365, 97, 0),

    /**
     * Twin-engine heavy-lift helicopter.
     * @ass1
     */
    SIKORSKY_SKYCRANE(AircraftType.HELICOPTER, 8724, 3328, 0, 9100);

    /**
     * Type of aircraft.
     * @ass1
     */
    public final AircraftType type;

    /**
     * Weight of aircraft with no load or fuel, in kilograms.
     * @ass1
     */
    public final int emptyWeight;

    /**
     * Maximum amount of fuel able to be carried, in litres.
     * @ass1
     */
    public final double fuelCapacity;

    /**
     * Maximum number of passengers able to be carried.
     * @ass1
     */
    public final int passengerCapacity;

    /**
     * Maximum amount of freight able to be carried, in kilograms.
     * @ass1
     */
    public final int freightCapacity;

    /**
     * Creates a new AircraftCharacteristics enum constant.
     *
     * @param type type of aircraft
     * @param emptyWeight empty weight
     * @param fuelCapacity maximum amount of fuel
     * @param passengerCapacity maximum number of passengers
     * @param freightCapacity maximum amount of freight
     */
    AircraftCharacteristics(AircraftType type, int emptyWeight, int fuelCapacity,
            int passengerCapacity, int freightCapacity) {
        this.type = type;
        this.emptyWeight = emptyWeight;
        this.fuelCapacity = fuelCapacity;
        this.passengerCapacity = passengerCapacity;
        this.freightCapacity = freightCapacity;
    }
}
