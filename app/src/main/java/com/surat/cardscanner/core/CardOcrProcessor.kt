package com.surat.cardscanner.core

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.surat.cardscanner.model.CardResult

internal class CardOcrProcessor(confirmFrameCount: Int = 3) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val aggregator = MultiFrameAggregator(confirmFrameCount)

    private var lastScanTime = 0L
    private val scanIntervalMs = 300L

    fun processFrame(frame: Bitmap, onResult: (CardResult) -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastScanTime < scanIntervalMs) return
        lastScanTime = now

        val overlayRegion = cropToOverlayRegion(frame)
        Log.d(TAG, "frame: ${frame.width}x${frame.height} → overlay: ${overlayRegion.width}x${overlayRegion.height}")

        val panZone = CardDetector.cropPanRegion(overlayRegion)
        Log.d(TAG, "cropPanRegion → ${panZone.width}x${panZone.height}")

        runOcr(panZone, onResult)
    }

    private fun runOcr(bitmap: Bitmap, onResult: (CardResult) -> Unit) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val lines = visionText.textBlocks
                    .flatMap { it.lines }
                    .map { it.text }

                Log.d(TAG, "OCR cəmi ${lines.size} sətir tapıldı:")
                for ((index, line) in lines.withIndex()) {
                    Log.d(TAG, "  -> Sətir $index: \"$line\"")
                }

                for (line in lines) {
                    val confirmed = tryCandidate(LuhnValidator.extractPANs(line), onResult)
                    if (confirmed) return@addOnSuccessListener
                }

                val groups = lines.flatMap { FOUR_DIGIT_GROUP.findAll(it).map(MatchResult::value) }
                if (groups.size >= 4) {
                    Log.d(TAG, "OCR 4-rəqəmli qruplar: $groups")
                    for (i in 0..groups.size - 4) {
                        val candidate = groups.subList(i, i + 4).joinToString(" ")
                        val confirmed = tryCandidate(LuhnValidator.extractPANs(candidate), onResult)
                        if (confirmed) return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OCR xəta: ${e.message}")
            }
    }

    private fun tryCandidate(
        pans: List<String>,
        onResult: (CardResult) -> Unit,
    ): Boolean {
        if (pans.isEmpty()) return false
        Log.d(TAG, "PAN namizədi: ${pans.first()}")
        val confirmed = aggregator.addCandidate(pans.first())
        if (confirmed != null) {
            Log.d(TAG, "Aggregator təsdiqlədi: $confirmed")
            aggregator.reset()
            onResult(CardResult(pan = confirmed))
            return true
        }
        return false
    }

    private fun cropToOverlayRegion(frame: Bitmap): Bitmap {
        val w = frame.width
        val h = frame.height
        val overlayW = (w * 0.88f).toInt()
        val overlayH = (overlayW / 1.586f).toInt()
        val left = ((w - overlayW) / 2f).toInt()
        val top = ((h - overlayH) / 2f).toInt()
        if (top < 0 || left < 0 || left + overlayW > w || top + overlayH > h) return frame
        return Bitmap.createBitmap(frame, left, top, overlayW, overlayH)
    }

    fun reset() = aggregator.reset()

    fun close() = recognizer.close()

    companion object {
        private const val TAG = "CardOcrProcessor"
        private val FOUR_DIGIT_GROUP = Regex("[0-9]{4}")
    }
}
