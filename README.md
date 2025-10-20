# VehSense Android App

## Overview

**VehSense** is an *Android App* that cooperates with *ELM327* controller, connected to *OBD-II port* of 2000+ vehicles and provides the information to the *veh-sense-backend*. 
The **VehSense** is based on standard OBD-II PIDs defined by SAE J1979.

- The main target of the VehSense are drivers with moderately new vehicles (year 2000+)
- The aim of the VehSense is to rate each individual drive when VehSense App open and connected with ELM327
- The App provides storage of the data for the driver to inspect and learn from to make sure the drives are the most economic!
- The App support both individual users and corporational with fleet management!

## Current stage of the development:
Creating working, report sending and fully functional App with minimal UI (for now). Current focus is to implement new CAN PIDs, encapsulate them into the ObdFrame data class and send through http methods to backend.

## Golden thoughts: 
- Use the *Engine Load* at 0x04 PID to estimate the usage of an engine. The info is passed in % and is much better than simple throttle position, since it includes info from MAF + Throttle Position and some other... Should be tracked in
- Track 0x0D Vehicle speed regression - large changes in short amount of time indicate that the driver slams the brakes.
- Include: MAX SPEED, AVG SPEED (BOTH ARE EASY TO IMPLEMENT)
- Also include: history of speed. create a map with each step and increment selected one if the current speed falls into it. This will give an information of most popular range of speed in given drive.