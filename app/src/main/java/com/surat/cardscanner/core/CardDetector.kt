package com.surat.cardscanner.core

import android.graphics.Bitmap

internal object CardDetector {

    fun cropPanRegion(cardBitmap: Bitmap): Bitmap {
        val w          = cardBitmap.width
        val h          = cardBitmap.height
        val top        = (h * 0.35).toInt()
        val cropHeight = (h * 0.40).toInt()
        val left       = (w * 0.05).toInt()
        val cropWidth  = (w * 0.90).toInt()
        return Bitmap.createBitmap(
            cardBitmap,
            left.coerceAtLeast(0),
            top.coerceAtLeast(0),
            cropWidth.coerceAtMost(w - left),
            cropHeight.coerceAtMost(h - top)
        )
    }
}
