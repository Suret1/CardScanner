package com.surat.cardscanner

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.os.ConfigurationCompat
import com.surat.cardscanner.model.CardResult
import com.surat.cardscanner.model.ScannerConfig
import com.surat.cardscanner.ui.CardScannerScreen

class CardScannerActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val appLocale = ConfigurationCompat
            .getLocales(newBase.applicationContext.resources.configuration)[0]
        if (appLocale == null) {
            super.attachBaseContext(newBase)
            return
        }
        val config = Configuration(newBase.resources.configuration).apply {
            setLocale(appLocale)
        }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle     = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        val config = intent.getParcelableExtraCompat(EXTRA_CONFIG, ScannerConfig::class.java)
            ?: ScannerConfig()

        setContent {
            CardScannerScreen(
                config   = config,
                onResult = { result -> finishWithResult(result) },
                onBack   = { setResult(RESULT_CANCELED); finish() },
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
