package com.surat.cardscanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val ScannerBackground  = Color(0xFF0D0D0D)
internal val ScannerSurface     = Color(0xFF1A1A2E)
internal val ScannerPrimary     = Color(0xFF00C853)
internal val ScannerOnPrimary   = Color(0xFF003300)
internal val ScannerSecondary   = Color(0xFF64FFDA)
internal val ScannerError       = Color(0xFFFF5252)
internal val ScannerOnBackground = Color(0xFFEEEEEE)
internal val OverlayDark        = Color(0xCC000000)

private val DarkColorScheme = darkColorScheme(
    primary      = ScannerPrimary,
    onPrimary    = ScannerOnPrimary,
    secondary    = ScannerSecondary,
    background   = ScannerBackground,
    surface      = ScannerSurface,
    onBackground = ScannerOnBackground,
    onSurface    = ScannerOnBackground,
    error        = ScannerError,
)

@Composable
internal fun CardScannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content     = content,
    )
}
