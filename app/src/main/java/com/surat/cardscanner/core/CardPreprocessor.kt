package com.surat.cardscanner.core

import android.graphics.Bitmap
import com.surat.cardscanner.model.CardType
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import androidx.core.graphics.createBitmap

internal object CardPreprocessor {

    fun preprocess(bitmap: Bitmap): Pair<Bitmap, CardType> {
        val cardType = detectCardType(bitmap)
        val processed = when (cardType) {
            CardType.EMBOSSED -> preprocessEmbossed(bitmap)
            else              -> preprocessPrinted(bitmap)
        }
        return processed to cardType
    }

    fun preprocessEmbossed(bitmap: Bitmap): Bitmap {
        val mat         = bitmapToMat(bitmap)
        val gray        = Mat()
        val claheResult = Mat()
        val blurSharp   = Mat()
        val sharpened   = Mat()
        val blurred     = Mat()
        val thresh      = Mat()
        val kernel      = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0, 2.0))
        val morphed     = Mat()
        return try {
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)

            val clahe = Imgproc.createCLAHE(3.0, Size(8.0, 8.0))
            clahe.apply(gray, claheResult)

            Imgproc.GaussianBlur(claheResult, blurSharp, Size(0.0, 0.0), 3.0)
            Core.addWeighted(claheResult, 1.5, blurSharp, -0.5, 0.0, sharpened)

            Imgproc.GaussianBlur(sharpened, blurred, Size(3.0, 3.0), 0.0)

            Imgproc.adaptiveThreshold(
                blurred, thresh, 255.0,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY,
                15, 8.0
            )

            Imgproc.morphologyEx(thresh, morphed, Imgproc.MORPH_CLOSE, kernel)
            matToBitmap(morphed)
        } finally {
            mat.release(); gray.release(); claheResult.release()
            blurSharp.release(); sharpened.release(); blurred.release()
            thresh.release(); kernel.release(); morphed.release()
        }
    }

    fun preprocessPrinted(bitmap: Bitmap): Bitmap {
        val mat       = bitmapToMat(bitmap)
        val gray      = Mat()
        val equalized = Mat()
        val blurred   = Mat()
        val thresh    = Mat()
        return try {
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
            Imgproc.equalizeHist(gray, equalized)
            Imgproc.GaussianBlur(equalized, blurred, Size(3.0, 3.0), 0.0)
            Imgproc.threshold(
                blurred, thresh, 0.0, 255.0,
                Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU
            )
            matToBitmap(thresh)
        } finally {
            mat.release(); gray.release(); equalized.release()
            blurred.release(); thresh.release()
        }
    }

    fun detectCardType(bitmap: Bitmap): CardType {
        return try {
            val mat      = bitmapToMat(bitmap)
            val gray     = Mat()
            val gradX    = Mat()
            val gradY    = Mat()
            val magnitude = Mat()
            val stdDev   = MatOfDouble()
            try {
                Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
                Imgproc.Sobel(gray, gradX, CvType.CV_64F, 1, 0, 3)
                Imgproc.Sobel(gray, gradY, CvType.CV_64F, 0, 1, 3)
                Core.magnitude(gradX, gradY, magnitude)
                Core.meanStdDev(magnitude, MatOfDouble(), stdDev)
                if (stdDev.toArray()[0] > 25.0) CardType.EMBOSSED else CardType.PRINTED
            } finally {
                mat.release(); gray.release(); gradX.release()
                gradY.release(); magnitude.release(); stdDev.release()
            }
        } catch (e: Exception) {
            CardType.PRINTED
        }
    }

    private fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        val rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(rgba, mat)
        rgba.recycle()
        return mat
    }

    private fun matToBitmap(mat: Mat): Bitmap {
        val result = createBitmap(mat.cols(), mat.rows())
        Utils.matToBitmap(mat, result)
        return result
    }
}