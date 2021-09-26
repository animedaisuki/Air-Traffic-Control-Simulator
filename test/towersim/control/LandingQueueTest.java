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
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;

import java.util.List;

import static org.junit.Assert.*;

public class LandingQueueTest {

    private LandingQueue queue1;
    private LandingQueue queue2;
    private LandingQueue queue3;
    private LandingQueue queue4;
    private LandingQueue queue5;
    private Aircraft passengerAircraft1;
    private Aircraft passengerAircraft2;
    private Aircraft passengerAircraft3;
    private Aircraft passengerAircraftAway;
    private Aircraft passengerAircraftTakingOff;
    private Aircraft passengerAircraftLanding1;
    private Aircraft freightAircraftLanding2;
    private Aircraft freightAircraftLanding3;
    private Aircraft freightAircraftLanding4;
    private Aircraft freightAircraftLanding5;
    private Aircraft passengerAircraftLoading;
    private Aircraft passengerAircraftLoadingSingleTick;
    private Aircraft freightAircraftLoadingMultipleTicks;

    @Before
    public void setup() {
        this.queue1 = new LandingQueue();
        this.queue2 = new LandingQueue();
        this.queue3 = new LandingQueue();
        this.queue4 = new LandingQueue();
        this.queue5 = new LandingQueue();

        TaskList taskList1 = new TaskList(List.of(
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT)));

        TaskList taskList2 = new TaskList(List.of(
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 50),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT)));

        TaskList taskList3 = new TaskList(List.of(
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 35),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT)));

        TaskList taskListTakeoff = new TaskList(List.of(
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100)));

        TaskList taskListLand1 = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY)));

        TaskList taskListLand2 = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY)));

        TaskList taskListLand3 = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY)));

        TaskList taskListLand4 = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY)));

        TaskList taskListLand5 = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY)));

        TaskList taskListLand6 = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY)));

        TaskList taskListLoad = new TaskList(List.of(
                new Task(TaskType.LOAD, 70),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT)));

        TaskList taskListAway = new TaskList(List.of(
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 70),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY)));

        this.passengerAircraft1 = new PassengerAircraft("ABC001",
                AircraftCharacteristics.AIRBUS_A320,
                taskList1,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity / 10, 0);

        this.passengerAircraft2 = new PassengerAircraft("ABC002",
                AircraftCharacteristics.AIRBUS_A320,
                taskList2,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity / 2, 0);

        this.passengerAircraft3 = new PassengerAircraft("ABC003",
                AircraftCharacteristics.ROBINSON_R44,
                taskList3,
                AircraftCharacteristics.ROBINSON_R44.fuelCapacity / 2, 0);

        this.passengerAircraftTakingOff = new PassengerAircraft("TAK001",
                AircraftCharacteristics.AIRBUS_A320,
                taskListTakeoff,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity / 2, 100);

        this.passengerAircraftLanding1 = new PassengerAircraft("LAN011",
                AircraftCharacteristics.AIRBUS_A320,
                taskListLand1,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity / 2, 100);

        this.freightAircraftLanding2 = new FreightAircraft("LOD012",
                AircraftCharacteristics.BOEING_747_8F,
                taskListLand2,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity / 2, 0);

        this.freightAircraftLanding3 = new FreightAircraft("LOD013",
                AircraftCharacteristics.BOEING_747_8F,
                taskListLand2,
                100, 0);

        this.freightAircraftLanding4 = new FreightAircraft("LOD014",
                AircraftCharacteristics.BOEING_747_8F,
                taskListLand2,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity / 2, 0);

        this.freightAircraftLanding5 = new FreightAircraft("LOD015",
                AircraftCharacteristics.BOEING_747_8F,
                taskListLand2,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity / 2, 0);

        this.passengerAircraftLoading = new PassengerAircraft("LOD001",
                AircraftCharacteristics.AIRBUS_A320,
                taskListLoad,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity / 8, 0);

        this.passengerAircraftLoadingSingleTick =
                new PassengerAircraft("LOD002",
                        AircraftCharacteristics.ROBINSON_R44,
                        taskListLoad, // current task is LOAD @ 70%
                        AircraftCharacteristics.ROBINSON_R44.fuelCapacity / 2,
                        0);

        this.freightAircraftLoadingMultipleTicks = new FreightAircraft("LOD003",
                AircraftCharacteristics.BOEING_747_8F,
                taskListLoad, // current task is LOAD @ 70%
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity / 2, 0);

        this.passengerAircraftAway = new PassengerAircraft("AWY001",
                AircraftCharacteristics.AIRBUS_A320,
                taskListAway,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity, 120);
    }

    @Test
    public void addAircraftTest1() {
        System.out.println(queue1.toString());
        assertEquals("LandingQueue []", queue1.toString());
    }

    @Test
    public void addAircraftTest2() {
        queue1.addAircraft(passengerAircraftLanding1);
        assertEquals("LandingQueue [LAN011]", queue1.toString());
    }

    @Test
    public void containsAircraftTest1() {
        assertEquals(false,
                queue1.containsAircraft(passengerAircraftLanding1));
    }

    @Test
    public void containsAircraftTest2() {
        queue1.addAircraft(passengerAircraftLanding1);
        assertEquals(true,
                queue1.containsAircraft(passengerAircraftLanding1));
    }

    @Test
    public void peekAircraftTest1() {
        //peek aircraft which fuel amount less than 20 percent
        queue1.addAircraft(passengerAircraftLanding1);
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(freightAircraftLanding3);
        assertEquals("AIRPLANE LOD013 BOEING_747_8F LAND",
                queue1.peekAircraft().toString());
    }

    @Test
    public void peekAircraftTest2() {
        //peek aircraft with emergency
        queue1.addAircraft(passengerAircraftLanding1);
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(freightAircraftLanding3);
        freightAircraftLanding2.declareEmergency();
        assertEquals("AIRPLANE LOD012 BOEING_747_8F LAND (EMERGENCY)",
                queue1.peekAircraft().toString());
    }

    @Test
    public void peekAircraftTest3() {
        //peek passenger aircraft
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(passengerAircraftLanding1);
        assertEquals("AIRPLANE LAN011 AIRBUS_A320 LAND",
                queue1.peekAircraft().toString());
    }

    @Test
    public void peekAircraftTest4() {
        //peek the first aircraft
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(freightAircraftLanding4);
        queue1.addAircraft(freightAircraftLanding5);
        assertEquals("AIRPLANE LOD012 BOEING_747_8F LAND",
                queue1.peekAircraft().toString());
    }

    @Test
    public void removeAircraftTest1() {
        //test when the queue is empty
        assertEquals(null, queue1.removeAircraft());
    }

    @Test
    public void removeAircraftTest2() {
        //test when aircraft which fuel amount less than 20 percent
        queue1.addAircraft(passengerAircraftLanding1);
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(freightAircraftLanding3);
        assertEquals("AIRPLANE LOD013 BOEING_747_8F LAND",
                queue1.removeAircraft().toString());
    }

    @Test
    public void removeAircraftTest3() {
        //test when aircraft with emergency
        queue1.addAircraft(passengerAircraftLanding1);
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(freightAircraftLanding3);
        freightAircraftLanding2.declareEmergency();
        assertEquals("AIRPLANE LOD012 BOEING_747_8F LAND (EMERGENCY)",
                queue1.peekAircraft().toString());
    }

    @Test
    public void removeAircraftTest4() {
        //test for passenger aircraft
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(passengerAircraftLanding1);
        assertEquals("AIRPLANE LAN011 AIRBUS_A320 LAND",
                queue1.removeAircraft().toString());
    }

    @Test
    public void removeAircraftTest5() {
        //test the first aircraft
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(freightAircraftLanding4);
        queue1.addAircraft(freightAircraftLanding5);
        assertEquals("AIRPLANE LOD012 BOEING_747_8F LAND",
                queue1.removeAircraft().toString());
    }

    @Test
    public void getAircraftInOrder() {
        //test when the queue is empty
        assertEquals("[]", queue1.getAircraftInOrder().toString());
    }

    @Test
    public void rgetAircraftInOrder2() {
        //test when aircraft which fuel amount less than 20 percent
        queue1.addAircraft(passengerAircraftLanding1);
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(freightAircraftLanding3);
        assertEquals(
                "[AIRPLANE LOD013 BOEING_747_8F LAND, AIRPLANE LAN011 "
                        + "AIRBUS_A320 LAND, AIRPLANE LOD012 BOEING_747_8F LAND]",
                queue1.getAircraftInOrder().toString());
    }

    @Test
    public void getAircraftInOrder3() {
        //test when aircraft with emergency
        queue1.addAircraft(passengerAircraftLanding1);
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(freightAircraftLanding3);
        freightAircraftLanding2.declareEmergency();
        assertEquals(
                "[AIRPLANE LOD012 BOEING_747_8F LAND (EMERGENCY), "
                        + "AIRPLANE LOD013 BOEING_747_8F LAND, "
                        + "AIRPLANE LAN011 AIRBUS_A320 LAND]",
                queue1.getAircraftInOrder().toString());
    }

    @Test
    public void getAircraftInOrder4() {
        //test for passenger aircraft
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(passengerAircraftLanding1);
        assertEquals("[AIRPLANE LAN011 AIRBUS_A320 LAND, "
                        + "AIRPLANE LOD012 BOEING_747_8F LAND]",
                queue1.getAircraftInOrder().toString());
    }

    @Test
    public void getAircraftInOrderTest5() {
        //test the first aircraft
        queue1.addAircraft(freightAircraftLanding2);
        queue1.addAircraft(freightAircraftLanding4);
        queue1.addAircraft(freightAircraftLanding5);
        assertEquals("[AIRPLANE LOD012 BOEING_747_8F LAND, "
                        + "AIRPLANE LOD014 BOEING_747_8F LAND, "
                        + "AIRPLANE LOD015 BOEING_747_8F LAND]",
                queue1.getAircraftInOrder().toString());
    }
}
