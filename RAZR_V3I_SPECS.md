# Motorola Razr V3i Reference Specifications

Reference source: `Razr V3i References/Razr V3i Specs.docx`, supplied with the project.

## Industrial design

- Form factor: ultra-thin clamshell flip phone
- Height: 98.0 mm
- Width: 53.0 mm
- Thickness: 13.9 mm
- Weight: 100 g
- Reference finish: Silver Quartz; regional variants include purple and gold

## Displays

- Main display: 2.2-inch TFT color display
- Main resolution: 176 x 220 pixels
- Main color depth: up to 262,144 colors
- External display: CSTN color display
- External resolution: 96 x 80 pixels
- External color depth: up to 65,536 colors

## Hardware and multimedia

- Internal storage: 10 MB
- Expandable storage: microSD, up to 512 MB
- RAM: 10 MB
- Rear camera: 1.23 MP digital camera with digital zoom and video capture/playback
- Front camera: none; outer mirror/lens supports self-portraits using the external display
- Audio: MP3/MP4 player, polyphonic ringtones, MP3 ringtones
- Included reference ringtone: `app/src/main/res/raw/razr_v3_original.mp3`

## Connectivity

- Cellular: quad-band GSM 850 / 900 / 1800 / 1900 MHz
- Network generation: 2G
- SIM: single mini-SIM
- Bluetooth: 1.2
- Wired connection: mini-USB 2.0 for data, charging, and headset use
- Wi-Fi: not supported
- GPS: not supported

## Battery

- Battery: removable 710 mAh lithium-ion
- Talk time: up to 3 hours 30 minutes
- Standby time: up to 200 hours

## Android integration notes

Mode 7 uses these references for visual proportions and styling. Android's native ringtone picker remains the source of truth for selecting call ringtones. The supplied MP3 is packaged as an app sound resource and can be exposed as a selectable ringtone in a later settings pass. Native AlarmClock intents are used for alarms and timers.