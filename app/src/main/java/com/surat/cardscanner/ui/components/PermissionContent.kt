package com.surat.cardscanner.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.surat.cardscanner.ui.theme.ScannerBackground
import com.surat.cardscanner.ui.theme.ScannerOnBackground
import com.surat.cardscanner.ui.theme.ScannerPrimary
import com.surat.cardscanner.ui.theme.ScannerSurface
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun CameraPermissionHandler(onGranted: @Composable () -> Unit) {
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    when {
        permissionState.status.isGranted -> onGranted()
        permissionState.status.shouldShowRationale ->
            PermissionRationaleDialog(
                onConfirm = { permissionState.launchPermissionRequest() },
                onDismiss = { },
            )
        else -> PermissionDeniedContent(
            isPermanentlyDenied = !permissionState.status.shouldShowRationale &&
                    !permissionState.status.isGranted,
            onRequestPermission = { permissionState.launchPermissionRequest() },
        )
    }
}

@Composable
private fun PermissionRationaleDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = ScannerSurface,
        iconContentColor  = ScannerPrimary,
        titleContentColor = ScannerOnBackground,
        textContentColor  = ScannerOnBackground.copy(alpha = 0.8f),
        icon = {
            Icon(
                imageVector        = Icons.Outlined.CameraAlt,
                contentDescription = null,
                modifier           = Modifier.size(40.dp),
            )
        },
        title = { Text("Kamera İcazəsi") },
        text  = {
            Text(
                text      = "Kart skan etmək üçün kameraya giriş icazəsi lazımdır.",
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors  = ButtonDefaults.textButtonColors(contentColor = ScannerPrimary),
            ) { Text("İcazə ver", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors  = ButtonDefaults.textButtonColors(contentColor = ScannerOnBackground.copy(alpha = 0.6f)),
            ) { Text("İmtina et") }
        },
    )
}

@Composable
private fun PermissionDeniedContent(isPermanentlyDenied: Boolean, onRequestPermission: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier           = Modifier.fillMaxSize().background(ScannerBackground),
        contentAlignment   = Alignment.Center,
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth().padding(24.dp),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = ScannerSurface),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column(
                modifier              = Modifier.padding(28.dp),
                horizontalAlignment   = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector        = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint               = ScannerPrimary,
                    modifier           = Modifier.size(56.dp),
                )
                Text(
                    text       = if (isPermanentlyDenied) "Kamera icazəsi deaktivdir" else "Kamera icazəsi lazımdır",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = ScannerOnBackground,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                )
                Text(
                    text      = if (isPermanentlyDenied)
                        "Tətbiq parametrlərindən kamera icazəsini aktivləşdirin."
                    else
                        "Kartı skan etmək üçün kameraya giriş icazəsi lazımdır.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = ScannerOnBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                if (isPermanentlyDenied) {
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        },
                        colors   = ButtonDefaults.buttonColors(containerColor = ScannerPrimary),
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Parametrləri aç", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    }
                } else {
                    Button(
                        onClick  = onRequestPermission,
                        colors   = ButtonDefaults.buttonColors(containerColor = ScannerPrimary),
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("İcazə ver", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    }
                }
            }
        }
    }
}
