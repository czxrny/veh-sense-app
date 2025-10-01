# VehSense Android App

## Overview

**VehSense** is an *Android App* that cooperates with *ELM327* controller, connected to *OBD-II port* of 2000+ vehicles and provides the information to the *veh-sense-backend*. 
- The main target of the VehSense are drivers with moderately new vehicles (year 2000+)
- The aim of the VehSense is to rate each individual drive when VehSense App open and connected with ELM327
- The App provides storage of the data for the driver to inspect and learn from to make sure the drives are the most economic!
- The App support both individual users and corporational with fleet management!

## Current stage of the development:
Creating working, report sending and fully functional App with minimal UI (for now). Current focus is to implement new CAN PIDs, encapsulate them into the ObdFrame data class and send through http methods to backend.
