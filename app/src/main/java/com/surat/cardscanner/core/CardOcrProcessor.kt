package com.surat.cardscanner.core

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.surat.cardscanner.model.CardResult
import com.surat.cardscanner.model.CardType

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

        val cardRegion = CardDetector.findCardRegion(overlayRegion)
        if (cardRegion == null) {
            Log.d(TAG, "findCardRegion → tapılmadı, embossed fallback cəhdi")
            val panFallback = CardDetector.cropPanRegion(overlayRegion)
            runOcr(CardPreprocessor.preprocessEmbossed(panFallback), CardType.EMBOSSED, "fallback-embossed", onResult)
            runOcr(panFallback, CardType.UNKNOWN, "fallback-raw", onResult)
            return
        }
        Log.d(TAG, "findCardRegion → tapıldı: ${cardRegion.width}x${cardRegion.height}")

        val panZone = CardDetector.cropPanRegion(cardRegion)
        Log.d(TAG, "cropPanRegion → ${panZone.width}x${panZone.height}")

        val (processed, cardType) = CardPreprocessor.preprocess(panZone)
        Log.d(TAG, "detectCardType → $cardType")

        runOcr(processed, cardType, "processed", onResult)
        runOcr(panZone, CardType.UNKNOWN, "original", onResult)
    }

    private fun runOcr(bitmap: Bitmap, cardType: CardType, label: String, onResult: (CardResult) -> Unit) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val lines = visionText.textBlocks
                    .flatMap { it.lines }
                    .map { it.text }

                Log.d(TAG, "OCR[$label] cəmi ${lines.size} sətir tapıldı:")
                for ((index, line) in lines.withIndex()) {
                    Log.d(TAG, "  -> Sətir $index: \"$line\"")
                }

                val fixedLines = lines.map { fixEmbossed(it) }

                for (fixed in fixedLines) {
                    val confirmed = tryCandidate(LuhnValidator.extractPANs(fixed), cardType, label, onResult)
                    if (confirmed) return@addOnSuccessListener
                }

                val groups = fixedLines.flatMap { FOUR_DIGIT_GROUP.findAll(it).map(MatchResult::value) }
                if (groups.size >= 4) {
                    Log.d(TAG, "OCR[$label] 4-rəqəmli qruplar: $groups")
                    for (i in 0..groups.size - 4) {
                        val candidate = groups.subList(i, i + 4).joinToString(" ")
                        val confirmed = tryCandidate(LuhnValidator.extractPANs(candidate), cardType, label, onResult)
                        if (confirmed) return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OCR[$label] xəta: ${e.message}")
            }
    }

    private fun fixEmbossed(text: String): String = buildString(text.length) {
        for (c in text) {
            append(
                when (c) {
                    'O', 'o', 'Q' -> '0'
                    'I', 'l', '|' -> '1'
                    'Z', 'z'      -> '2'
                    'A'           -> '4'
                    'S', 's'      -> '5'
                    'G'           -> '6'
                    'T'           -> '7'
                    'B'           -> '8'
                    'g', 'q'      -> '9'
                    else          -> c
                }
            )
        }
    }

    private fun tryCandidate(
        pans: List<String>,
        cardType: CardType,
        label: String,
        onResult: (CardResult) -> Unit,
    ): Boolean {
        if (pans.isEmpty()) return false
        Log.d(TAG, "OCR[$label] PAN namizədi: ${pans.first()}")
        val confirmed = aggregator.addCandidate(pans.first())
        if (confirmed != null) {
            Log.d(TAG, "OCR[$label] Aggregator təsdiqlədi: $confirmed")
            aggregator.reset()
            onResult(CardResult(pan = confirmed, cardType = cardType))
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
