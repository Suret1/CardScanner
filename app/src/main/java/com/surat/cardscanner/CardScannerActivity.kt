package com.surat.cardscanner

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.surat.cardscanner.model.CardResult
import com.surat.cardscanner.model.ScannerConfig
import com.surat.cardscanner.ui.CardScannerScreen
import java.util.Locale

class CardScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        val config = intent.getParcelableExtraCompat(EXTRA_CONFIG, ScannerConfig::class.java)
            ?: ScannerConfig()

        val localizedContext = buildLocalizedContext()
        val localizedConfig = localizedContext.resources.configuration

        setContent {
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfig,
            ) {
                CardScannerScreen(
                    config = config,
                    onResult = { result -> finishWithResult(result) },
                    onBack = { setResult(RESULT_CANCELED); finish() },
                )
            }
        }
    }

    private fun buildLocalizedContext(): Context {
        val language = intent.getStringExtra(EXTRA_LOCALE)?.takeIf { it.isNotEmpty() }
            ?: Locale.getDefault().language
        val locale = Locale(language)
        val overrideConfig = Configuration(resources.configuration)
        overrideConfig.setLocales(LocaleList(locale))
        return createConfigurationContext(overrideConfig)
    }

    private fun finishWithResult(result: CardResult) {
        val data = Intent().apply { putExtra(EXTRA_RESULT, result) }
        setResult(RESULT_OK, data)
        finish()
    }

    companion object {
        const val EXTRA_CONFIG = "com.surat.cardscanner.EXTRA_CONFIG"
        const val EXTRA_RESULT = "com.surat.cardscanner.EXTRA_RESULT"
        const val EXTRA_LOCALE = "com.surat.cardscanner.EXTRA_LOCALE"
    }
}