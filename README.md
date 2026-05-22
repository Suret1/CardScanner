# CardScanner

An Android library for scanning **printed** credit/debit cards using ML Kit

[![](https://jitpack.io/v/Suret1/CardScanner.svg)](https://jitpack.io/#Suret1/CardScanner)

---

## Features

- Jetpack Compose UI with edge-to-edge support
- Compatible with View-based (legacy) Android apps via `ActivityResultContract`
- Vertical PAN detection — reads cards where the number is printed top-to-bottom
- Camera permission flow with automatic system dialog and Settings fallback
- Flash toggle
- Luhn validation
- Multi-frame confirmation to eliminate false positives
- Localized UI — Azerbaijani, Russian, English (follows system language)

---

## Installation

### 1. Add JitPack to `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

### 2. Add the dependency

```kotlin
dependencies {
    implementation("com.github.Suret1:CardScanner:1.0.0")
}
```

---

## Usage

### Jetpack Compose

```kotlin
val launcher = rememberLauncherForActivityResult(CardScannerContract()) { result ->
    result?.let { card ->
        println(card.formattedPan)   // "4111 1111 1111 1111"
        println(card.cardBrand)      // VISA
    }
}

Button(onClick = { launcher.launch(ScannerConfig()) }) {
    Text("Scan card")
}
```

### Fragment

```kotlin
private val scanLauncher = registerForActivityResult(CardScannerContract()) { result ->
    result?.let { binding.panView.text = it.formattedPan }
}

scanButton.setOnClickListener {
    scanLauncher.launch(ScannerConfig(confirmFrameCount = 2))
}
```

### Activity

```kotlin
private val scanLauncher = registerForActivityResult(CardScannerContract()) { result ->
    result?.let { panEditText.setText(it.formattedPan) }
}

scanButton.setOnClickListener {
    scanLauncher.launch(ScannerConfig())
}
```

---

## ScannerConfig

```kotlin
ScannerConfig(
    showFlashButton   = true,  // show/hide flash toggle button
    confirmFrameCount = 3,     // consecutive frames required to confirm a PAN
    vibrateOnSuccess  = true,  // haptic feedback on successful scan
)
```

| Parameter | Type | Default | Description |
|---|---|---|---|
| `showFlashButton` | Boolean | `true` | Show the flash toggle button |
| `confirmFrameCount` | Int | `3` | Consecutive frames that must agree on the same PAN. Lower = faster, higher = fewer false positives |
| `vibrateOnSuccess` | Boolean | `true` | Short vibration when a card is successfully scanned |

---

## CardResult

```kotlin
data class CardResult(
    val pan: String,            // "4111111111111111"
    val formattedPan: String,   // "4111 1111 1111 1111"
    val cardBrand: CardBrand,   // VISA | MASTERCARD | AMEX | DISCOVER | UNKNOWN
    val cardType: CardType,     // PRINTED | UNKNOWN
)
```

---

## How it works

```
CameraX frame  (every 300 ms)
       │
       ▼
cropToOverlayRegion()        — crops the visible card area from the frame
       │
       ├──► original orientation
       ├──► rotated 90° CW    — handles PANs printed top-to-bottom
       └──► rotated 90° CCW   — handles PANs printed bottom-to-top
              │
              ▼
       CardDetector.cropPanRegion()   — isolates the middle 40% strip (PAN zone)
              │
              ▼
       ML Kit TextRecognition
              │
              ▼
       LuhnValidator.extractPANs()    — validates candidates with Luhn algorithm
              │
              ▼
       MultiFrameAggregator           — confirms after N consistent frames
              │
              ▼
       CardResult callback
```

---

## Requirements

| Requirement | Version |
|---|---|
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |
| Kotlin | 2.0+ |
| Compose BOM | 2026.05+ |
