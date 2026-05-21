package com.surat.cardscanner.core

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import androidx.core.graphics.createBitmap

internal object CardDetector {

    private const val CARD_RATIO_MIN = 1.3f
    private const val CARD_RATIO_MAX = 1.9f

    fun findCardRegion(bitmap: Bitmap): Bitmap? {
        val mat       = Mat()
        val gray      = Mat()
        val blurred   = Mat()
        val edged     = Mat()
        val kernel    = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(7.0, 7.0))
        val dilated   = Mat()
        val hierarchy = Mat()
        return try {
            val copy = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            Utils.bitmapToMat(copy, mat)
            copy.recycle()

            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
            Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
            Imgproc.Canny(blurred, edged, 50.0, 150.0)
            Imgproc.dilate(edged, dilated, kernel)

            val contours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(
                dilated, contours, hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
            )

            val imageArea = mat.rows() * mat.cols()
            val cardContour = contours
                .filter { Imgproc.contourArea(it) > imageArea * 0.1 }
                .maxByOrNull { Imgproc.contourArea(it) }
                ?: return null

            val rect = Imgproc.boundingRect(cardContour)
            val ratio = rect.width.toFloat() / rect.height
            if (ratio < CARD_RATIO_MIN || ratio > CARD_RATIO_MAX) return null

            if (rect.x < 0 || rect.y < 0 ||
                rect.x + rect.width > mat.cols() ||
                rect.y + rect.height > mat.rows()
            ) return null

            val roi = Mat(mat, rect)
            val cropped = createBitmap(roi.cols(), roi.rows())
            Utils.matToBitmap(roi, cropped)
            roi.release()
            cropped
        } catch (e: Exception) {
            null
        } finally {
            mat.release(); gray.release(); blurred.release()
            edged.release(); kernel.release(); dilated.release(); hierarchy.release()
        }
    }

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
