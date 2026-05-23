package com.surat.cardscanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.surat.cardscanner.model.CardResult
import com.surat.cardscanner.model.ScannerConfig
import com.surat.cardscanner.ui.CardScannerScreen

class CardScannerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        val config = intent.getParcelableExtraCompat(EXTRA_CONFIG, ScannerConfig::class.java)
            ?: ScannerConfig()

        setContent {
            CardScannerScreen(
                config = config,
                onResult = { result -> finishWithResult(result) },
                onBack = { setResult(RESULT_CANCELED); finish() },
            )
        }
    }

    private fun finishWithResult(result: CardResult) {
        val data = Intent().apply { putExtra(EXTRA_RESULT, result) }
        setResult(RESULT_OK, data)
        finish()
    }

    companion object {
        const val EXTRA_CONFIG = "com.surat.cardscanner.EXTRA_CONFIG"
        const val EXTRA_RESULT = "com.surat.cardscanner.EXTRA_RESULT"
    }

}
