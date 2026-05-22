# CardScanner Library

**Printed** kredit kartlarını ML Kit ilə skan edən Android library.

- ✅ Jetpack Compose ilə yazılıb
- ✅ View-based (legacy) Android app-lərlə uyğun
- ✅ Yalnız ML Kit — OpenCV asılılığı yoxdur
- ✅ Kamera icazəsi idarəsi + Settings yönlənmə
- ✅ Flash on/off
- ✅ Luhn validasiyası
- ✅ Multi-frame təsdiqləmə (false positive azaldır)

---

## Quraşdırma

### 1. `settings.gradle.kts`-ə repo əlavə et
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }  // JitPack istifadə edirsinizsə
        // yaxud local:
        // maven { url = uri("../cardscanner-lib") }
    }
}
```

### 2. `build.gradle.kts`-ə dependency əlavə et
```kotlin
dependencies {
    implementation("com.github.YourUser:cardscanner:1.0.0")
    // yaxud local module:
    // implementation(project(":cardscanner-lib"))
}
```

---

## İstifadə

### Compose app-də
```kotlin
@Composable
fun PaymentScreen() {
    val launcher = rememberLauncherForActivityResult(CardScannerContract()) { result ->
        result?.let { card ->
            println("PAN: ${card.pan}")
            println("Formatlanmış: ${card.formattedPan}")  // "1234 5678 9012 3456"
            println("Kart markası: ${card.cardBrand}")     // VISA, MASTERCARD...
        }
    }

    Button(
        onClick = {
            launcher.launch(
                ScannerConfig(
                    title              = "Ödəniş kartı",
                    showFlashButton    = true,
                    confirmFrameCount  = 3,
                    vibrateOnSuccess   = true,
                )
            )
        }
    ) {
        Text("Kartı skan et")
    }
}
```

---

### View-based (Fragment) app-də
```kotlin
class PaymentFragment : Fragment() {

    private val scanLauncher = registerForActivityResult(CardScannerContract()) { result ->
        result?.let { card ->
            binding.panTextView.text = card.formattedPan
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scanButton.setOnClickListener {
            scanLauncher.launch(ScannerConfig())
        }
    }
}
```

### View-based (Activity) app-də
```kotlin
class PaymentActivity : AppCompatActivity() {

    private val scanLauncher = registerForActivityResult(CardScannerContract()) { result ->
        result?.let { card ->
            panEditText.setText(card.formattedPan)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        scanButton.setOnClickListener {
            scanLauncher.launch(
                ScannerConfig(title = "Kartı skan edin")
            )
        }
    }
}
```

---

## ScannerConfig parametrləri

| Parametr            | Tip     | Default          | Açıqlama                              |
|---------------------|---------|------------------|---------------------------------------|
| `title`             | String  | "Kartı skan edin"| Scan ekranının başlığı                |
| `showFlashButton`   | Boolean | true             | Flash button göstərilsin?             |
| `confirmFrameCount` | Int     | 3                | Nə qədər ardıcıl frame eyni PAN verməlidir |
| `vibrateOnSuccess`  | Boolean | true             | Uğurlu scan-da vibrasiya              |

---

## CardResult modeli

```kotlin
data class CardResult(
    val pan: String,           // "1234567890123456"
    val formattedPan: String,  // "1234 5678 9012 3456"
    val cardBrand: CardBrand,  // VISA | MASTERCARD | AMEX | DISCOVER | UNKNOWN
)
```

---

## Texniki arxitektura

```
CameraX Frame (300ms interval)
        │
        ▼
cropToOverlayRegion()         ← Ekranda göstərilən kart sahəsi
        │
        ▼
CardDetector.cropPanRegion()  ← Kartın 35-75% hündürlüyü (PAN sahəsi)
        │
        ▼
ML Kit TextRecognition OCR
        │
        ▼
LuhnValidator.extractPANs()   ← Luhn alqoritmi ilə doğrulama
        │
        ▼
MultiFrameAggregator (3x confirm)
        │
        ▼
CardResult callback
```

---

## Minimum tələblər

- Android minSdk: **26** (Android 8.0)
- targetSdk: **36**
- Kotlin: 2.0+
- Compose BOM: 2026.05+
