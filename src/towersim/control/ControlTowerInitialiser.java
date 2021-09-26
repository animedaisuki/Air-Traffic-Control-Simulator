package towersim.control;

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
import towersim.util.MalformedSaveException;
import towersim.util.NoSpaceException;

import java.io.*;
import java.util.*;

/**
 * Utility class that contains static methods for loading a control tower and
 * associated entities from files.
 */
public class ControlTowerInitialiser extends Object {

    /**
     * Construct ControlTowerInitialiser
     */
    public ControlTowerInitialiser() {

    }

    /**
     * Loads the number of ticks elapsed from the given reader instance.
     *
     * @param reader reader from which to load the number of ticks elapsed
     * @return number of ticks elapsed
     * @throws MalformedSaveException if the format of the text read from the
     *                                reader is invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from
     *                                the reader
     */
    public static long loadTick(Reader reader) throws MalformedSaveException,
            IOException {
        BufferedReader br = new BufferedReader(reader);
        //if the number of ticks elapsed is not an integer throw exception
        try {
            long numTicks = Long.parseLong(br.readLine());
            //if The number of ticks elapsed is less than zero throw exception
            if (numTicks < 0) {
                throw new MalformedSaveException();
            }
            return numTicks;
        } catch (NumberFormatException e) {
            throw new MalformedSaveException(e);
        }
    }

    /**
     * Loads the list of all aircraft managed by the control tower from the
     * given reader instance.
     *
     * @param reader reader from which to load the list of aircraft
     * @return list of aircraft read from the reader
     * @throws IOException            if an IOException is encountered when reading from
     *                                the reader
     * @throws MalformedSaveException if the format of the text read from the
     *                                reader is invalid according to the rules above
     */
    public static List<Aircraft> loadAircraft(Reader reader)
            throws IOException, MalformedSaveException {
        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        //if The number of aircraft specified on the first line of the reader
        // is not an integer throw exception
        try {
            Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException();
        }
        int numAircrafts = Integer.parseInt(line);
        List<Aircraft> aircrafts = new ArrayList<>();
        if (numAircrafts > 0) {
            for (int i = 0; i < numAircrafts; i++) {
                //The number of aircraft specified on the first line is not
                // equal to the number of aircraft actually read from the
                // reader throw exception
                try {
                    aircrafts.add(readAircraft(br.readLine()));
                } catch (NullPointerException e) {
                    throw new MalformedSaveException(e);
                }
            }
        }
        //The number of aircraft specified on the first line is not
        // equal to the number of aircraft actually read from the
        // reader throw exception
        if (br.readLine() != null) {
            throw new MalformedSaveException();
        }
        return aircrafts;
    }

    /**
     * Loads the takeoff queue, landing queue and map of loading aircraft from
     * the given reader instance.
     *
     * @param reader          reader from which to load the queues and loading map
     * @param aircraft        list of all aircraft, used when validating that
     *                        callsigns exist
     * @param takeoffQueue    empty takeoff queue that aircraft will be added to
     * @param landingQueue    empty landing queue that aircraft will be added to
     * @param loadingAircraft empty map that aircraft and loading times will be
     *                        added to
     * @throws MalformedSaveException if the format of the text read from the
     *                                reader is invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from
     *                                the reader
     */
    public static void loadQueues(Reader reader, List<Aircraft> aircraft,
                                  TakeoffQueue takeoffQueue,
                                  LandingQueue landingQueue,
                                  Map<Aircraft, Integer> loadingAircraft)
            throws MalformedSaveException, IOException {
        BufferedReader br = new BufferedReader(reader);
        readQueue(br, aircraft, takeoffQueue);
        readQueue(br, aircraft, landingQueue);
        readLoadingAircraft(br, aircraft, loadingAircraft);
    }

    /**
     * Loads the list of terminals and their gates from the given reader
     * instance.
     *
     * @param reader   reader from which to load the list of terminals and their
     *                 gates
     * @param aircraft list of all aircraft, used when validating that callsigns
     *                 exist
     * @return list of terminals (with their gates) read from the reader
     * @throws MalformedSaveException if the format of the text read from the
     *                                reader is invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading
     *                                from the reader
     */
    public static List<Terminal> loadTerminalsWithGates(Reader reader,
                                                        List<Aircraft> aircraft)
            throws MalformedSaveException, IOException {
        BufferedReader br = new BufferedReader(reader);
        List<Terminal> terminals = new ArrayList<>();
        String firstline = br.readLine();
        // if The number of terminals specified at the top of the file is not
        // an integer throw exception
        try {
            Integer.parseInt(firstline);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException(e);
        }
        int numTerminal = Integer.parseInt(firstline);
        for (int i = 0; i < numTerminal; i++) {
            //if The number of terminals specified is not equal to the number of
            // terminals actually read from the reader throw exception
            try {
                terminals.add(readTerminal(br.readLine(), br, aircraft));
            } catch (NullPointerException e) {
                throw new MalformedSaveException(e);
            }
        }
        //if The number of terminals specified is not equal to the number of
        // terminals actually read from the reader throw exception
        if (br.readLine() != null) {
            throw new MalformedSaveException();
        }
        return terminals;
    }

    /**
     * Reads an aircraft from its encoded representation in the given string.
     *
     * @param line line of text containing the encoded aircraft
     * @return decoded aircraft instance
     * @throws MalformedSaveException if the format of the given string is
     *                                invalid according to the rules above
     */
    public static Aircraft readAircraft(String line)
            throws MalformedSaveException {
        String contentBefore = line;
        //if More colons (:) are detected at the end in the string than
        // expected throw exception
        if (contentBefore.endsWith(":")) {
            throw new MalformedSaveException();
        }
        String[] content = contentBefore.split(":");
        //if More/fewer colons (:) are detected in the string than expected
        // throw exception
        if (content.length > 6) {
            throw new MalformedSaveException();
        }
        String callsign = content[0];
        String characteristics = content[1];
        //if The aircraft's AircraftCharacteristics is not valid throw exception
        try {
            AircraftCharacteristics aircraftCharacteristics =
                    AircraftCharacteristics.valueOf(characteristics);
        } catch (IllegalArgumentException e) {
            throw new MalformedSaveException();
        }
        String taskList = content[2];
        TaskList tasks = readTaskList(taskList);
        //if The aircraft's fuel amount is not a double throw exception
        try {
            Double.parseDouble(content[3]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException(e);
        }
        double fuel = Double.parseDouble(content[3]);
        //The aircraft's fuel amount is less than zero or greater than the
        // aircraft's maximum fuel capacity throw exception
        if (fuel < 0 || fuel > AircraftCharacteristics
                .valueOf(characteristics).fuelCapacity) {
            throw new MalformedSaveException();
        }
        String hasEmergency = content[4];
        //if The amount of cargo (freight/passengers) onboard the aircraft is
        // not an integer throw exception
        try {
            Integer.parseInt(content[5]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException();
        }
        int numCargo = Integer.parseInt(content[5]);
        //The amount of cargo (freight/passengers) onboard the aircraft is
        // less than zero throw exception
        if (numCargo < 0) {
            throw new MalformedSaveException();
        }
        if (AircraftCharacteristics.valueOf(characteristics).freightCapacity
                > 0) {
            FreightAircraft freightAircraft = new FreightAircraft(callsign,
                    AircraftCharacteristics.valueOf(characteristics), tasks,
                    fuel, numCargo);
            //if The amount of cargo (freight/passengers) onboard the
            // aircraft is greater than the aircraft's maximum freight
            // capacity throw exception
            if (numCargo > freightAircraft
                    .getCharacteristics().freightCapacity) {
                throw new MalformedSaveException();
            }
            if (hasEmergency.equals("true")) {
                freightAircraft.declareEmergency();
            }
            return freightAircraft;
        }
        PassengerAircraft passengerAircraft = new PassengerAircraft(callsign,
                AircraftCharacteristics.valueOf(characteristics), tasks, fuel,
                numCargo);
        //if The amount of cargo (freight/passengers) onboard the
        // aircraft is greater than the aircraft's maximum passenger
        // capacity throw exception
        if (numCargo > passengerAircraft
                .getCharacteristics().passengerCapacity) {
            throw new MalformedSaveException();
        }
        if (hasEmergency.equals("true")) {
            passengerAircraft.declareEmergency();
        }
        return passengerAircraft;
    }

    /**
     * Reads a task list from its encoded representation in the given string.
     *
     * @param taskListPart string containing the encoded task list
     * @return decoded task list instance
     * @throws MalformedSaveException if the format of the given string is
     *                                invalid according to the rules above
     */
    public static TaskList readTaskList(String taskListPart)
            throws MalformedSaveException {
        String[] list = taskListPart.split(",");
        List<Task> tasks = new ArrayList<>();
        for (String string : list) {
            //Check when task type is Load (and throw exception for the case
            // that a task is not Load but have at-symbol (@))
            if (string.contains("@")) {
                //if More than one at-symbol (@) is detected at the end for any
                // task in the task list throw exception
                if (string.endsWith("@")) {
                    throw new MalformedSaveException();
                }
                String[] loadTaskContent = string.split("@");
                //if More than one at-symbol (@) is detected for any task in
                // the task list throw exception
                if (loadTaskContent.length > 2) {
                    throw new MalformedSaveException();
                }
                String loadType = loadTaskContent[0];
                //if More an at-symbol (@) is detected for any task
                // which is not load task, or the task list's TaskType is not
                // valid (not equals Load) in the task list throw exception
                if (!loadType.equals("LOAD")) {
                    throw new MalformedSaveException();
                }
                try {
                    //if A task's load percentage is not an integer throw
                    // exception
                    int loadPercent = Integer.parseInt(loadTaskContent[1]);
                } catch (NumberFormatException e) {
                    throw new MalformedSaveException();
                }
                int loadPercent = Integer.parseInt(loadTaskContent[1]);
                // if A task's load percentage is less than zero throw exception
                if (loadPercent < 0) {
                    throw new MalformedSaveException();
                }
                Task task = new Task(TaskType.LOAD, loadPercent);
                tasks.add(task);
                //Check when task type is not Load
            } else {
                try {
                    //The task list's TaskType is not valid
                    TaskType taskType = TaskType.valueOf(string);
                } catch (IllegalArgumentException e) {
                    throw new MalformedSaveException();
                }
                TaskType taskType = TaskType.valueOf(string);
                Task task = new Task(taskType);
                tasks.add(task);
            }
        }
        //if he task list is invalid according to the rules specified in
        // TaskList(List) throw exception
        try {
            return new TaskList(tasks);
        } catch (IllegalArgumentException e) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Reads an aircraft queue from the given reader instance.
     *
     * @param reader   reader from which to load the aircraft queue
     * @param aircraft list of all aircraft, used when validating that
     *                 callsigns exist
     * @param queue    empty queue that aircraft will be added to
     * @throws IOException            if an IOException is encountered when
     *                                reading from the reader
     * @throws MalformedSaveException if the format of the text read from the
     *                                reader is invalid according to the rules
     *                                above
     */
    public static void readQueue(BufferedReader reader,
                                 List<Aircraft> aircraft, AircraftQueue queue)
            throws IOException, MalformedSaveException {
        String typeAndNumBefore = reader.readLine();
        //if The first line contains more/fewer colons (:) than expected at
        // the end throw exception
        if (typeAndNumBefore.endsWith(":")) {
            throw new MalformedSaveException();
        }
        //if The first line read from the reader is null throw exception
        if (typeAndNumBefore == null) {
            throw new MalformedSaveException();
        }
        String[] typeAndNum = typeAndNumBefore.split(":");
        //if The first line contains more/fewer colons (:) than expected at
        // throw exception
        if (typeAndNum.length > 2) {
            throw new MalformedSaveException();
        }
        String type = typeAndNum[0];
        //if The queue type specified in the first line is not equal to the
        // simple class name of the queue provided as a parameter throw
        // exception
        if (!queue.getClass().getSimpleName().equals(type)) {
            throw new MalformedSaveException();
        }
        //if The number of aircraft specified on the first line is not an
        // integer throw exception
        try {
            Integer.parseInt(typeAndNum[1]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException(e);
        }
        int num = Integer.parseInt(typeAndNum[1]);
        if (num > 0) {
            List<Aircraft> matched = new ArrayList<>();
            List<String> callsinChecker = new ArrayList<>();
            String callsingsBefore = reader.readLine();
            //if The number of aircraft specified is greater than zero and
            // the second line read is null throw exception
            if (callsingsBefore == null) {
                throw new MalformedSaveException();
            }
            String[] callsigns = callsingsBefore.split(",");
            for (String callsign : callsigns) {
                callsinChecker.add(callsign);
            }
            //if The number of callsigns listed on the second line is not
            // equal to the number of aircraft specified on the first line
            // throw exception
            if (callsinChecker.size() != num) {
                throw new MalformedSaveException();
            }
            for (Aircraft aircraft1 : aircraft) {
                for (String callsign : callsigns) {
                    if (aircraft1.getCallsign().equals(callsign)) {
                        matched.add(aircraft1);
                        if (queue.getClass().getSimpleName().equals(type)) {
                            queue.addAircraft(aircraft1);
                        }
                    }
                }
            }
            //if A callsign listed on the second line does not correspond to
            // the callsign of any aircraft contained in the list of aircraft
            // given as a parameter throw exception
            if (matched.size() != callsinChecker.size()) {
                throw new MalformedSaveException();
            }
        }
    }

    /**
     * Reads the map of currently loading aircraft from the given reader
     * instance.
     *
     * @param reader          reader from which to load the map of loading aircraft
     * @param aircraft        list of all aircraft, used when validating that
     *                        callsigns exist
     * @param loadingAircraft empty map that aircraft and their loading times
     *                        will be added to
     * @throws IOException            if an IOException is encountered when reading from
     *                                the reader
     * @throws MalformedSaveException if the format of the text read from the
     *                                reader is invalid according to the rules above
     */
    public static void readLoadingAircraft(BufferedReader reader,
                                           List<Aircraft> aircraft,
                                           Map<Aircraft, Integer> loadingAircraft)
            throws IOException, MalformedSaveException {
        String contentBefore = reader.readLine();
        //if The number of colons (:) detected on the first line at the end
        // is more than expected throw exception
        if (contentBefore.endsWith(":")) {
            throw new MalformedSaveException();
        }
        //if he first line read from the reader is null throw exception
        if (contentBefore == null) {
            throw new MalformedSaveException();
        }
        String[] content = contentBefore.split(":");
        //The number of colons (:) detected on the first line is more/fewer
        // than expected throw exception
        if (content.length > 2) {
            throw new MalformedSaveException();
        }
        String task = content[0];
        //if The number of aircraft specified on the first line is not an
        // integer throw exception
        try {
            Integer.parseInt(content[1]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException(e);
        }
        int numAircraft = Integer.parseInt(content[1]);
        String aircraftsBefore = reader.readLine();
        //if The number of aircraft is greater than zero and the second line
        // read from the reader is null throw exception
        if (numAircraft > 0 && aircraftsBefore == null) {
            throw new MalformedSaveException();
        }
        List<Aircraft> matched = new ArrayList<>();
        if (numAircraft > 0) {
            List<String> callsings = new ArrayList<>();
            String[] aircrafts = aircraftsBefore.split(",");
            //if The number of aircraft specified on the first line is not equal
            // to the number of callsigns read on the second line throw
            // exception
            if (numAircraft != aircrafts.length) {
                throw new MalformedSaveException();
            }
            for (int i = 0; i < numAircraft; i++) {
                String singleAirBefore = aircrafts[i];
                //if For any callsign/loading time pair on the second line,
                // the number of colons detected is not equal to one at the
                // end throw exception
                if (singleAirBefore.endsWith(":")) {
                    throw new MalformedSaveException();
                }
                String[] singleAir = aircrafts[i].split(":");
                //if For any callsign/loading time pair on the second line,
                // the number of colons detected is not equal to one throw
                // exception
                if (singleAir.length > 2) {
                    throw new MalformedSaveException();
                }
                String callsign = singleAir[0];
                callsings.add(callsign);
                //if Any ticksRemaining value on the second line is not an
                // integer throw exception
                try {
                    Integer.parseInt(singleAir[1]);
                } catch (NumberFormatException e) {
                    throw new MalformedSaveException(e);
                }
                int time = Integer.parseInt(singleAir[1]);
                //if Any ticksRemaining value on the second line is less than
                // one throw exception
                if (time < 1) {
                    throw new MalformedSaveException();
                }
                for (Aircraft air : aircraft) {
                    if (air.getCallsign().equals(callsign)) {
                        matched.add(air);
                        loadingAircraft.put(air, time);
                    }
                }
            }
            //if A callsign listed on the second line does not correspond to
            // the callsign of any aircraft contained in the list of aircraft
            // given as a parameter throw exception
            if (matched.size() != callsings.size()) {
                throw new MalformedSaveException();
            }
        }
    }

    /**
     * Reads a terminal from the given string and reads its gates from the
     * given reader instance.
     *
     * @param line     string containing the first line of the encoded terminal
     * @param reader   reader from which to load the gates of the terminal
     *                 (subsequent lines)
     * @param aircraft list of all aircraft, used when validating that
     *                 callsigns exist
     * @return decoded terminal with its gates added
     * @throws IOException            if an IOException is encountered when reading from
     *                                the reader
     * @throws MalformedSaveException if the format of the text read from the
     *                                reader is invalid according to the rules above
     */
    public static Terminal readTerminal(String line, BufferedReader reader,
                                        List<Aircraft> aircraft)
            throws IOException, MalformedSaveException {
        String terminalContentBefore = line;
        //if The number of colons (:) detected on the first line is
        // more than expected at the end throw exception
        if (terminalContentBefore.endsWith(":")) {
            throw new MalformedSaveException();
        }
        String[] terminalContent = terminalContentBefore.split(":");
        //if The number of colons (:) detected on the first line is
        // more/fewer than expected throw exception
        if (terminalContent.length > 4) {
            throw new MalformedSaveException();
        }
        String type = terminalContent[0];
        //if The terminal type specified on the first line is neither
        // AirplaneTerminal nor HelicopterTerminal throw exception
        // AirplaneTerminal nor HelicopterTerminal throw exception
        if (!type.equals("AirplaneTerminal") && !type.equals(
                "HelicopterTerminal")) {
            throw new MalformedSaveException();
        }
        //if The terminal number is not an integer throw exception
        try {
            Integer.parseInt(terminalContent[1]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException();
        }
        int terminalNumber = Integer.parseInt(terminalContent[1]);
        //if The terminal number is less than one throw exception
        if (terminalNumber < 1) {
            throw new MalformedSaveException();
        }
        String emergency = terminalContent[2];
        //if The number of gates in the terminal is not an integer throw
        // exception
        try {
            Integer.parseInt(terminalContent[3]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException(e);
        }
        int numGates = Integer.parseInt(terminalContent[3]);
        if (type.equals("AirplaneTerminal")) {
            AirplaneTerminal terminal = new AirplaneTerminal(terminalNumber);
            //if The number of gates is less than zero or is greater than
            // Terminal's max number of gates throw exception
            if (numGates < 0 || numGates > 6) {
                throw new MalformedSaveException();
            }
            if (numGates > 0) {
                for (int i = 0; i < numGates; i++) {
                    try {
                        String encodeGate = reader.readLine();
                        //if A line containing an encoded gate was expected,
                        // but EOF (end of file) was received throw exception
                        if (encodeGate == null) {
                            throw new MalformedSaveException();
                        }
                        terminal.addGate(readGate(encodeGate, aircraft));
                    } catch (NoSpaceException e) {
                        //ignore
                    }
                }
            }
            if (emergency.equals("true")) {
                terminal.declareEmergency();
            }
            return terminal;
        } else {
            HelicopterTerminal terminal =
                    new HelicopterTerminal(terminalNumber);
            //if The number of gates is less than zero or is greater than
            // Terminal's max number of gates throw exception
            if (numGates < 0 || numGates > 6) {
                throw new MalformedSaveException();
            }
            if (numGates > 0) {
                for (int i = 0; i < numGates; i++) {
                    try {
                        String encodeGate = reader.readLine();
                        //if A line containing an encoded gate was expected,
                        // but EOF (end of file) was received throw exception
                        if (encodeGate == null) {
                            throw new MalformedSaveException();
                        }
                        terminal.addGate(readGate(encodeGate, aircraft));
                    } catch (NoSpaceException e) {
                        //ignore
                    }
                }
            }
            if (emergency.equals("true")) {
                terminal.declareEmergency();
            }
            return terminal;
        }
    }

    /**
     * Reads a gate from its encoded representation in the given string.
     *
     * @param line     string containing the encoded gate
     * @param aircraft list of all aircraft, used when validating that
     *                 callsigns exist
     * @return decoded gate instance
     * @throws MalformedSaveException if the format of the given string is
     *                                invalid according to the rules above
     */
    public static Gate readGate(String line, List<Aircraft> aircraft)
            throws MalformedSaveException {
        String contentBefore = line;
        // if The number of colons (:) detected was more/fewer than expected
        // at the end throw exception
        if (contentBefore.endsWith(":")) {
            throw new MalformedSaveException();
        }
        String[] content = contentBefore.split(":");
        //if The number of colons (:) detected was more/fewer than expected
        // throw exception
        if (content.length > 2) {
            throw new MalformedSaveException();
        }
        //if The gate number is not an integer throw exception
        try {
            Integer.parseInt(content[0]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException(e);
        }
        int gateNum = Integer.parseInt(content[0]);
        //if The gate number is less than one throw exception
        if (gateNum < 1) {
            throw new MalformedSaveException();
        }
        String airplane = content[1];
        Gate gate = new Gate(gateNum);
        List<Aircraft> macthedAir = new ArrayList<>();
        for (Aircraft singleAir : aircraft) {
            if (singleAir.getCallsign().equals(airplane)) {
                macthedAir.add(singleAir);
            }
        }
        if (macthedAir.isEmpty()) {
            //if The callsign of the aircraft parked at the gate is not empty
            // and the callsign does not correspond to the callsign of any
            // aircraft contained in the list of aircraft given as a
            // parameter throw exception
            if (!airplane.equals("empty")) {
                throw new MalformedSaveException();
            }
        } else {
            for (Aircraft singleAir : macthedAir) {
                try {
                    gate.parkAircraft(singleAir);
                } catch (NoSpaceException e) {
                    //ignore
                }
            }
        }
        return gate;
    }

    /**
     * Creates a control tower instance by reading various airport entities
     * from the given readers.
     *
     * @param tick               reader from which to load the number of ticks elapsed
     * @param aircraft           reader from which to load the list of aircraft
     * @param queues             reader from which to load the aircraft queues and map of
     *                           loading aircraft
     * @param terminalsWithGates reader from which to load the terminals and
     *                           their gates
     * @return control tower created by reading from the given readers
     * @throws MalformedSaveException if reading from any of the given readers
     *                                results in a MalformedSaveException, indicating
     *                                the contents of that reader are invalid
     * @throws IOException            if an IOException is encountered when reading from
     *                                any of the readers
     */
    public static ControlTower createControlTower(Reader tick, Reader aircraft,
                                                  Reader queues,
                                                  Reader terminalsWithGates)
            throws MalformedSaveException, IOException {
        long tickElapsed = loadTick(tick);
        List<Aircraft> aircrafts = new ArrayList<>(loadAircraft(aircraft));
        List<Terminal> terminals = new ArrayList<>(
                loadTerminalsWithGates(terminalsWithGates, aircrafts));
        TakeoffQueue takeoffQueue = new TakeoffQueue();
        LandingQueue landingQueue = new LandingQueue();
        Map<Aircraft, Integer> loadingAircraft =
                new TreeMap<>(Comparator.comparing(Aircraft::getCallsign));
        loadQueues(queues, aircrafts, takeoffQueue, landingQueue,
                loadingAircraft);
        ControlTower controlTower = new ControlTower(tickElapsed, aircrafts,
                landingQueue, takeoffQueue, loadingAircraft);
        for (Terminal terminal : terminals) {
            controlTower.addTerminal(terminal);
        }
        return controlTower;
    }
}
