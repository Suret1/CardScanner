package com.surat.cardscanner.ui

import android.graphics.Bitmap
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import com.surat.cardscanner.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
                    config = config,
                    isFlashOn = isFlashOn,
                    scanState = scanState,
                    onFlashToggle = { isFlashOn = !isFlashOn },
                    onBack = onBack,
                    onFrameReady = { bitmap ->
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
    val isCompleted = scanState is ScanState.Completed

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ScannerBackground)
    ) {
        val cardW = maxWidth * 0.88f
        val cardH = cardW * (1f / 1.586f)
        val cardTop = (maxHeight - cardH) * 0.5f
        val cardBottom = cardTop + cardH

        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            isFlashOn = isFlashOn,
            onFrameReady = onFrameReady,
        )

        ScanOverlay(
            modifier = Modifier.fillMaxSize(),
            isScanning = !isCompleted,
        )

        TopBar(
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(),
        )

        Text(
            text = stringResource(
                if (isCompleted) R.string.scanner_hint_done else R.string.scanner_hint_scanning
            ),
            color = ScannerOnBackground.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (72.dp + cardTop) * 0.5f)
                .padding(horizontal = 32.dp),
        )

        Text(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = cardBottom + 20.dp)
                .padding(horizontal = 32.dp),
            text = stringResource(R.string.scanner_verify),
            color = ScannerOnBackground.copy(alpha = 0.65f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )

        if (config.showFlashButton) {
            FlashButton(
                isOn = isFlashOn,
                onClick = onFlashToggle,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
            )
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.Transparent)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Geri",
                tint = ScannerOnBackground,
            )
        }
    }
}

private sealed class ScanState {
    data object Scanning : ScanState()
    data class Completed(val result: CardResult) : ScanState()
}

@Preview(name = "Scanning", showSystemUi = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewScannerContentScanning() {
    ScannerContent(
        config = ScannerConfig(),
        isFlashOn = false,
        scanState = ScanState.Scanning,
        onFlashToggle = {},
        onBack = {},
        onFrameReady = {},
    )
}
