package com.surat.cardscanner.ui

import android.graphics.Bitmap
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.surat.cardscanner.core.CardOcrProcessor
import com.surat.cardscanner.model.CardResult
import com.surat.cardscanner.model.ScannerConfig
import com.surat.cardscanner.ui.components.CameraPermissionHandler
import com.surat.cardscanner.ui.components.CameraPreview
import com.surat.cardscanner.ui.components.FlashButton
import com.surat.cardscanner.ui.components.ScanOverlay
import com.surat.cardscanner.ui.theme.CardScannerTheme
import com.surat.cardscanner.ui.theme.ScannerBackground
import com.surat.cardscanner.ui.theme.ScannerOnBackground
import com.surat.cardscanner.ui.theme.ScannerPrimary
import kotlinx.coroutines.delay

@Composable
internal fun CardScannerScreen(
    config: ScannerConfig,
    onResult: (CardResult) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var isFlashOn by remember { mutableStateOf(false) }
    var scanState by remember { mutableStateOf<ScanState>(ScanState.Scanning) }

    val processor = remember {
        CardOcrProcessor(confirmFrameCount = config.confirmFrameCount)
    }

    val onCardDetected: (CardResult) -> Unit = { result ->
        if (scanState !is ScanState.Completed) {
            if (config.vibrateOnSuccess) {
                @Suppress("DEPRECATION")
                (context.getSystemService(Vibrator::class.java))
                    ?.vibrate(VibrationEffect.createOneShot(120, 80))
            }
            scanState = ScanState.Completed(result)
        }
    }

    LaunchedEffect(scanState) {
        if (scanState is ScanState.Completed) {
            delay(350)
            onResult((scanState as ScanState.Completed).result)
        }
    }

    DisposableEffect(Unit) {
        onDispose { processor.close() }
    }

    CardScannerTheme {
        CameraPermissionHandler(
            onGranted = {
                ScannerContent(
                    config        = config,
                    isFlashOn     = isFlashOn,
                    scanState     = scanState,
                    onFlashToggle = { isFlashOn = !isFlashOn },
                    onBack        = onBack,
                    onFrameReady  = { bitmap ->
                        if (scanState is ScanState.Scanning) {
                            processor.processFrame(bitmap, onCardDetected)
                        }
                    },
                )
            }
        )
    }
}

@Composable
private fun ScannerContent(
    config: ScannerConfig,
    isFlashOn: Boolean,
    scanState: ScanState,
    onFlashToggle: () -> Unit,
    onBack: () -> Unit,
    onFrameReady: (Bitmap) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(ScannerBackground)) {

        CameraPreview(
            modifier     = Modifier.fillMaxSize(),
            isFlashOn    = isFlashOn,
            onFrameReady = onFrameReady,
        )

        ScanOverlay(
            modifier   = Modifier.fillMaxSize(),
            isScanning = scanState is ScanState.Scanning,
            hint       = when (scanState) {
                is ScanState.Scanning   -> "Kartı çərçivəyə yerləşdirin"
                is ScanState.Completed  -> "✓  Kart oxundu"
            },
        )

        TopBar(
            title    = config.title,
            onBack   = onBack,
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth(),
        )

        if (config.showFlashButton) {
            FlashButton(
                isOn     = isFlashOn,
                onClick  = onFlashToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 16.dp),
            )
        }

        AnimatedVisibility(
            visible  = scanState is ScanState.Completed,
            enter    = fadeIn() + scaleIn(),
            exit     = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            SuccessBadge(
                pan      = (scanState as? ScanState.Completed)?.result?.formattedPan ?: "",
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.Transparent)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector    = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Geri",
                tint           = ScannerOnBackground,
            )
        }
        Text(
            text      = title,
            style     = MaterialTheme.typography.titleMedium,
            color     = ScannerOnBackground,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SuccessBadge(pan: String, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = ScannerPrimary.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment      = Alignment.CenterVertically,
            horizontalArrangement  = Arrangement.spacedBy(12.dp),
        ) {
            Text("✓", style = MaterialTheme.typography.titleLarge, color = Color.Black)
            Column {
                Text(
                    text  = "Kart uğurla oxundu",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black.copy(alpha = 0.7f),
                )
                Text(
                    text       = pan,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = Color.Black,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private sealed class ScanState {
    data object Scanning : ScanState()
    data class Completed(val result: CardResult) : ScanState()
}
