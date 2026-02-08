# VehSense Android App

## Overview

**VehSense App** is a complete _Android App_ written in _Kotlin_ that cooperates with _ELM327 Scanner_ connected to the _OBD-II port_ of any ICE vehicle newer than year _2004_.

The **VehSense** uses the standard OBD-II PIDs defined by _SAE J1979 norm_, which are implemented in all of the vehicles with present OBD-II port.

Main **VehSense** project aim is to make _roads safer_, the _environment cleaner_, and _rides more economical_ by giving the driver a clear feedback after each individual ride made while the **VehSense** App is open and driver's device is connected to the _ELM327 Scanner_ through the _Bluetooth_.

## Network features

This application strictly cooperates with the [VehSense Backend](https://github.com/czxrny/veh-sense-backend.com) by:

- Implementing the _Account Management_ features.
- Providing an UI to perform _View, Edit, Add and Delete_ operations on User Vehicles stored in the database.
- Sending the _live data read from vehicle_ to create reports.
- Providing an UI to _View and Delete_ the created reports.
- Supporting both _individual_ and _corporational_ users with different permissions to _Vehicles and Reports_ data!

## Requirements

The **VehSense** app requires:

- An Android 8.0+ Smartphone.
- ICE Vehicle with an OBD-II port.
- An ELM327 Bluetooth Dongle (worth ~3$ on AliExpress).
- An internet connection.

## Getting Started

1. Clone the repository:  
```bash
git clone https://github.com/czxrny/veh-sense-app.git
```

2. Navigate to the main project directory:
```
cd ./veh-sense-app
```

3. Configure environment variables in .env.example:
```
nano ./.env.example
```

4. Copy .env.example to .env:
```
cp ./.env.example ./.env
```

5. Open the project in Android Studio.
6. Sync the `build.gradle.kts` file.
7. Build and deploy the app.

## How to use the App

1. Locate the OBD-II Port in your vehicle ([Klavkarr](https://www.klavkarr.com/location-plug-connector-obd.php) website might come in handy with that task).

2. Insert ELM327 interface into the OBD-II Port.

3. Start the Vehicle.

4. Select the device in the VehSense Mobile App.

5. Add your vehicle and select it as current in the Vehicles tab.

6. Connect to the device.

7. Start collecting the data.

## TODO
- [ ]  Refactor the `MainViewModel` by moving BluetoothSocket into new class.
- [ ]  Handle the _JWT Token Fetch_ in the `BackendCommunicator` rather than providing it.
- [ ] Create fake `ELMPoller` to enable testing.
- [ ] Handle data reading while the screen is disabled or app is not in focus.
- [ ]  Implement `Toasts` with app informations for better user experience.
- [ ]  Show notification when app is active.
- [ ]  UI Update.
