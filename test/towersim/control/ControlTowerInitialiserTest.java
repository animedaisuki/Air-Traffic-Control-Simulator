package towersim.control;

import org.junit.Before;
import org.junit.Test;
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
import towersim.util.NoSuitableGateException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class ControlTowerInitialiserTest {

    @Test(expected = MalformedSaveException.class)
    public void loadTerminalsWithGates_InvalidTest()
            throws IOException, MalformedSaveException {
        String fileContents = String.join(System.lineSeparator(),
                "1",
                "AirplaneTerminal:notATerminalNumber:false:0"
                // invalid terminal number
        );
        ControlTowerInitialiser.loadTerminalsWithGates(
                new StringReader(fileContents), List.of());
    }

    @Test
    public void readTaskList1()
            throws IOException, MalformedSaveException {
        String taskListPart = "LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT";
        assertEquals(taskListPart,
                ControlTowerInitialiser.readTaskList(taskListPart).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList2()
            throws IOException, MalformedSaveException {
        //test when the task type is invalid
        String taskListPart = "JAPAN," //invalid task type
                + "TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart);
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList3()
            throws IOException, MalformedSaveException {
        //test when load percentage is not an integer
        String taskListPart = "LOAD@JAPAN," //not an integer
                + "TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList4()
            throws IOException, MalformedSaveException {
        //test when load percentage is less than zero
        String taskListPart = "LOAD@-1," //integer less than 0
                + "TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList5()
            throws IOException, MalformedSaveException {
        //test when More than one at-symbol (@) is detected at beginning
        String taskListPart = "@LOAD@50," //extra @
                + "TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList6()
            throws IOException, MalformedSaveException {
        //test when More than one at-symbol (@) is detected in the mid position
        String taskListPart = "LOAD@@50," //extra @
                + "TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList7()
            throws IOException, MalformedSaveException {
        //test when More than one at-symbol (@) is detected at end
        String taskListPart = "LOAD@50@," //extra @
                + "TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList8()
            throws IOException, MalformedSaveException {
        //test when the order of task obeys the rules
        //after takeoff is not away
        String taskListPart = "TAKEOFF,"
                + "LOAD@50," //invalid argument
                + "TAKEOFF,AWAY,AWAY,AWAY,LAND,"
                + "WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList9()
            throws IOException, MalformedSaveException {
        //test when the order of task obeys the rules
        //after load is not takeoff
        String taskListPart = "LOAD@50,"
                + "AWAY," //invalid argument
                + "AWAY,AWAY,LAND,WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList10()
            throws IOException, MalformedSaveException {
        //test when the order of task obeys the rules
        //after wait is not wait or load
        String taskListPart = "LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT,"
                + "LAND," //invalid argument
                + " WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList11()
            throws IOException, MalformedSaveException {
        //test when the order of task obeys the rules
        //after land is not wait or load
        String taskListPart = "LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT,LAND,"
                + "LAND"//invalid argument
                + "WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test(expected = MalformedSaveException.class)
    public void readTaskList12()
            throws IOException, MalformedSaveException {
        //test when the order of task obeys the rules
        //after away is not away or land
        String taskListPart = "LOAD@50,TAKEOFF,AWAY,"
                + "TAKEOFF," //invalid argument
                + "AWAY,AWAY,LAND,"
                + "WAIT,LAND,WAIT";
        ControlTowerInitialiser.readTaskList(taskListPart).encode();
    }

    @Test
    public void readAircraft1()
            throws IOException, MalformedSaveException {
        String line = "ABC123:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:3250.00:false:100";
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft2()
            throws IOException, MalformedSaveException {
        //test when more colons are detected in the middle
        String line = "ABC123:"
                + ":"//invalid argument
                + "AIRBUS_A320:AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:3250.00:false:100";
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft3()
            throws IOException, MalformedSaveException {
        //test when more colons are detected at the beginning
        String line = ":"//invalid argument
                + "ABC123:"
                + "AIRBUS_A320:AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:3250.00:false:100";
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft4()
            throws IOException, MalformedSaveException {
        //test when more colons are detected at the end
        String line = "ABC123:"
                + "AIRBUS_A320:AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:3250.00:false:100"
                + ":";//invalid argument
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft5()
            throws IOException, MalformedSaveException {
        //test when fewer colons are detected
        String line = "ABC123"//invalid argument
                + "AIRBUS_A320:AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:3250.00:false:100";
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft6()
            throws IOException, MalformedSaveException {
        //test when The aircraft's AircraftCharacteristics is not valid
        String line = "ABC123:"
                + "JAPAN:" //invalid argument
                + "AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:3250.00:false:100";
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft7()
            throws IOException, MalformedSaveException {
        //test when The aircraft's fuel amount is not a double
        String line = "ABC123:"
                + "AIRBUS_A320:"
                + "AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:"
                + "JAPAN:" //invalid argument
                + "false:100";
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft8()
            throws IOException, MalformedSaveException {
        //test when The aircraft's fuel amount is less than zero
        String line = "ABC123:"
                + "AIRBUS_A320:"
                + "AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:"
                + "-1.00:" //invalid argument
                + "false:100";
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft9()
            throws IOException, MalformedSaveException {
        //test when The aircraft's fuel amount is greater than the aircraft's
        //maximum fuel capacity
        String line = "ABC123:"
                + "AIRBUS_A320:"
                + "AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:"
                + "9999999999999999999999999.00:" //invalid argument
                + "false:100";
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft10()
            throws IOException, MalformedSaveException {
        //test when he amount of cargo (freight/passengers) onboard the
        //aircraft is not an integer
        String line = "ABC123:"
                + "AIRBUS_A320:"
                + "AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:"
                + "100.00:"
                + "false:"
                + "JAPAN";//invalid argument
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft11()
            throws IOException, MalformedSaveException {
        //test when he amount of passengers onboard is less than zero
        String line = "ABC123:"
                + "AIRBUS_A320:"
                + "AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:"
                + "100.00:"
                + "false:"
                + "-1";//invalid argument
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft12()
            throws IOException, MalformedSaveException {
        //test when he amount of passengers onboard is reater than the
        //aircraft's maximum passenger capacity
        String line = "ABC123:"
                + "AIRBUS_A320:"
                + "AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:"
                + "100.00:"
                + "false:"
                + "999999999999999999999999999";//invalid argument
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft13()
            throws IOException, MalformedSaveException {
        //test when he amount of freight onboard is less than 0
        String line = "ABC123:"
                + "BOEING_747_8F:"
                + "AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:"
                + "100.00:"
                + "false:"
                + "-1";//invalid argument
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test(expected = MalformedSaveException.class)
    public void readAircraft14()
            throws IOException, MalformedSaveException {
        //test when he amount of freight onboard is greater than the
        //aircraft's maximum freight capacity
        String line = "ABC123:"
                + "BOEING_747_8F:"
                + "AWAY,AWAY,LAND,WAIT,LOAD@50,"
                + "TAKEOFF,AWAY:"
                + "100.00:"
                + "false:"
                + "999999999999999999999999999";//invalid argument
        assertEquals(line, ControlTowerInitialiser.readAircraft(line).encode());
    }

    @Test
    public void loadAircraft1()
            throws IOException, MalformedSaveException {
        String fileContents = "4\n"
                + "QFA481:AIRBUS_A320:LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY,AWAY,AWAY:19040.00:false:90\n"
                + "UTD302:BOEING_787:AWAY,AWAY,LAND,WAIT,LOAD@100,TAKEOFF,AWAY:113585.40:false:242\n"
                + "UPS119:BOEING_747_8F:LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT:226117.00:false:22959\n"
                + "VH-BFK:ROBINSON_R44:AWAY,AWAY,LAND,WAIT,LOAD@5,TAKEOFF:190"
                + ".00:false:0";
        ControlTowerInitialiser.loadAircraft(
                new StringReader(fileContents));
    }

    @Test(expected = MalformedSaveException.class)
    public void loadAircraft2()
            throws IOException, MalformedSaveException {
        //test when the first line of the reader is not an integer
        String fileContents = "JAPAN\n"
                + "QFA481:AIRBUS_A320:LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY,AWAY,AWAY:19040.00:false:90\n"
                + "UTD302:BOEING_787:AWAY,AWAY,LAND,WAIT,LOAD@100,TAKEOFF,AWAY:113585.40:false:242\n"
                + "UPS119:BOEING_747_8F:LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND,WAIT:226117.00:false:22959\n"
                + "VH-BFK:ROBINSON_R44:AWAY,AWAY,LAND,WAIT,LOAD@5,TAKEOFF:190"
                + ".00:false:0";
        ControlTowerInitialiser.loadAircraft(
                new StringReader(fileContents));
    }

    @Test(expected = MalformedSaveException.class)
    public void loadAircraft3()
            throws IOException, MalformedSaveException {
        //test when The number of aircraft specified on the first line is
        // less than the number of aircraft actually read from the reader.
        String fileContents = "4\n"
                + "QFA481:AIRBUS_A320:LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY,AWAY,AWAY:19040.00:false:90\n"
                + "UTD302:BOEING_787:AWAY,AWAY,LAND,WAIT,LOAD@100,TAKEOFF,AWAY:113585.40:false:242\n"
                + "UPS119:BOEING_747_8F:LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND,"
                + "WAIT:226117.00:false:22959\n";
        ControlTowerInitialiser.loadAircraft(
                new StringReader(fileContents));
    }

    @Test(expected = MalformedSaveException.class)
    public void loadAircraft4()
            throws IOException, MalformedSaveException {
        //test when The number of aircraft specified on the first line is
        // more than the number of aircraft actually read from the reader.
        String fileContents = "2\n"
                + "QFA481:AIRBUS_A320:LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY,AWAY,AWAY:19040.00:false:90\n"
                + "UTD302:BOEING_787:AWAY,AWAY,LAND,WAIT,LOAD@100,TAKEOFF,AWAY:113585.40:false:242\n"
                + "UPS119:BOEING_747_8F:LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND,"
                + "WAIT:226117.00:false:22959\n";
        ControlTowerInitialiser.loadAircraft(
                new StringReader(fileContents));
    }
}
