# Airport Simulator
# Overview
The project aims to simulate a basic Air Traffic Control (ATC) system. It builds on an extensible core that includes functionalities like aircraft queuing, terminal management, and gate assignments. This advanced version introduces additional features such as persistent data storage, a simple graphical user interface (GUI), and event-based actions like emergency states and task scheduling.

# Features
# Aircraft Queues
Support for landing and takeoff queues
Ability to add or remove aircraft from queues
Capability to check the presence of an aircraft in a queue
Listing of all aircraft in the queue in order
# Control Tower
Manages actions like aircraft landing, takeoff, and queuing
Enhanced gate assignment logic
# Data Initialization
A control tower initializer class that loads information from data files
Support for loading elapsed ticks, aircraft information, queue states, and terminal/gate configurations
# User Interface
A simple GUI implemented with JavaFX
Event-based actions like declaring and clearing emergency states
Real-time information update for selected aircraft and suitable gate assignment
Persistent Data
Capability to save and load the state of the simulation
Loading data files for elapsed ticks, aircraft information, queue states, and terminal/gate configurations
# Usage
The main entry point of the application is the Launcher class located in the towersim package. This class initializes the tick count, aircraft data, queue data, and terminal/gate configurations and launches the GUI.

```bash
Copy code
javac Launcher.java
java Launcher
```

# Unit Tests
JUnit 4 tests for the LandingQueue class are located in LandingQueueTest.
Selected methods of ControlTowerInitialiser are tested in ControlTowerInitialiserTest.

# Requirements
Java 8 or above
JavaFX for GUI

# License
The code in this project is licensed under MIT license.
