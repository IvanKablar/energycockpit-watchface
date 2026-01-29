# EnergyCockpit WatchFace

A modern WearOS watchface for energy monitoring and electricity price visualization.

## Features

- **4 Complication Slots** for customizable data display
- **ColorRamp Support** for RANGED_VALUE complications (visualize price levels with color gradients)
- **Tibber Integration** - displays current electricity prices
- Clean, minimal design optimized for WearOS

## Complication Slots

1. **Top Left** - SHORT_TEXT / RANGED_VALUE
2. **Top Right** - LONG_TEXT / SHORT_TEXT
3. **Bottom Left** - RANGED_VALUE with ColorRamp support (green→yellow→red gradient)
4. **Bottom Right** - MONOCHROMATIC_IMAGE

## Requirements

- WearOS 3.0+ (API 30+)
- Kotlin 2.1.0
- Android SDK 34

## Installation

### Debug Build
```bash
./gradlew :energycockpit:assembleDebug
adb install energycockpit/build/outputs/apk/debug/energycockpit-debug.apk
```

### Release Build
```bash
./gradlew :energycockpit:assembleRelease
```

## Development

Built with:
- Kotlin Multiplatform
- WearOS Watchface API
- Compose for Wear OS
- Canvas-based rendering with ColorRamp support

## License

Copyright © 2025 Ivan Kablar

## Version

v1.0 - Initial release
