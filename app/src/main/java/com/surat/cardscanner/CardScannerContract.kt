package com.surat.cardscanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.surat.cardscanner.model.CardResult
import com.surat.cardscanner.model.ScannerConfig

class CardScannerContract : ActivityResultContract<ScannerConfig, CardResult?>() {

    override fun createIntent(context: Context, input: ScannerConfig): Intent =
        Intent(context, CardScannerActivity::class.java).apply {
            putExtra(CardScannerActivity.EXTRA_CONFIG, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): CardResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtraCompat(CardScannerActivity.EXTRA_RESULT, CardResult::class.java)
    }
}
