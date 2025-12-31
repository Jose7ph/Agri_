# Baibars V9 SDK

Baibars V9 SDK is a software development kit designed for UAV flight control systems, providing interfaces and sample code for interacting with UAV flight controllers, remote controllers, and user interfaces.

## Table of Contents

- [Baibars V9 SDK](#baibars-v9-sdk)
  
  - [Project Structure](#project-structure)
  - [Documentation](#documentation)
  - [Functional Modules](#functional-modules)
  - [Upcoming Features](#upcoming-features)

    

### Project Structure

```
.
├── doc
│   └── v9-sdk.md          # V9 SDK detailed documentation
├── sdk
│   ├── README.md               # SDK release notes and version information
│   ├── jgcompose-v0.0.1.aar    # Compose UI AAR library
│   ├── v9sdk-v0.0.3.aar        # SDK AAR library (v0.0.3)
│   ├── v9sdk-v0.0.4.aar        # SDK AAR library (v0.0.4)
│   └── v9sdk-v0.0.5.aar        # SDK AAR library (v0.0.5) - Current version
├── firmware
│   ├── README.md                        # Firmware release notes
│   ├── V9_AG_FMU_APP_20251015VK.bin     # FMU firmware (20251015)
│   ├── V9_AG_PMU_APP_20251015.bin       # PMU firmware (20251015)
│   ├── V9_AG_FMU_APP_20251105VK.bin     # FMU firmware (20251105) - Current version
│   └── V9_AG_PMU_APP_20251105VK.bin     # PMU firmware (20251105) - Current version
└── README.md                            # Project documentation
```

## Documentation

For detailed interface descriptions, please refer to the following documents:

- [V9 SDK Documentation](./doc/v9-sdk.md)
- [V9 SDK Release Notes](./sdk/README.md)
- [Firmware Release Notes](./firmware/README.md)

## Functional Modules

### 1. Remote Controller

Provides interfaces for remote controller connection and control, including:

- Remote controller connection state management
- Remote controller parameter reading and setting
- Sending remote control data to flight controller
- Remote controller ID acquisition
- Channel mapping settings

Main interface: IController

### 2. Map UI

Provides map and visualization components needed for user interface development:

- Map display and control
- Drawing of markers (polygons, lines, markers, etc.)
- Map interaction event handling (click, long press, drag, etc.)
- Map view control

Main interface: IMapCanvas

### 3. Flight Controller

Provides interfaces for communication with the UAV flight control system, including:

- Flight control commands (takeoff, landing, return to home, etc.)
- Obstacle avoidance settings
- Route operation control (AB point operation, autonomous route, etc.)
- Parameter setting and reading
- Device calibration (compass, level calibration, etc.)
- Firmware upgrade
- Log reading

Main interface: VKAgProtocol



## Upcoming Features

We have already started developing the following features and plan to release them in future versions:

- [x] **Add M+ Mode**
- [x] **Added assist landing(need terrain radar)**
- [x] **Add landingifts up at the end of the route(need terrain radar)**
- [x] **Optimize Cleaning Mode**
- [x] **Optimize flight control**
- [x] **Optimize heading solution**
- [x] **Optimize lock detection**
- [ ] **Altitude - Meters to Feet converstion**
- [ ] **Radar distances - Meters to Feet converstion**