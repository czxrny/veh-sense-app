# VehSense Android App

## Overview

**VehSense** is a complete _Android App_ written in _Kotlin_ that cooperates with _ELM327 Scanner_ connected to the _OBD-II port_ of any ICE vehicle newer than year _2000_.

The **VehSense** uses the standard OBD-II PIDs defined by _SAE J1979 norm_, which are implemented in all of the vehicles with present OBD-II port.

## The goal of **VehSense App**

Main **VehSense** project aim is to make _roads safer_, the _environment cleaner_, and _rides more economical_ by giving the driver a clear feedback after each individual ride made while the **VehSense** App is open and driver's device is connected to the _ELM327 Scanner_ through the _Bluetooth_.

## Network features

This application strictly cooperates with the [VehSense Backend API](https://github.com/czxrny/veh-sense-backend.com) by:

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

## Current stage of the development:

Settle on which data should be passed to the api and if any of it should be analyzed in the app.

## Golden thoughts:

- Track 0x0D Vehicle speed regression - large changes in short amount of time indicate that the driver slams the brakes.
- Include: MAX SPEED, AVG SPEED (BOTH ARE EASY TO IMPLEMENT)
- Also include: history of speed. create a map with each step and increment selected one if the current speed falls into it. This will give an information of most popular range of speed in given drive.

## Getting Started

- Locate the OBD-II Port in your vehicle ([Klavkarr](https://www.klavkarr.com/location-plug-connector-obd.php) website might come in handy with that task)
- Insert ELM327 into the OBD-II Port.
- Start the Vehicle.
- Connect to device.
- Start collecting the data.
