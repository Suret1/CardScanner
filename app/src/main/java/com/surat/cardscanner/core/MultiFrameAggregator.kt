package com.surat.cardscanner.core

internal class MultiFrameAggregator(private val confirmFrameCount: Int = 3) {

    private val votes = mutableMapOf<String, Int>()

    fun addCandidate(pan: String?): String? {
        pan ?: return null
        val count = (votes[pan] ?: 0) + 1
        votes[pan] = count
        return if (count >= confirmFrameCount) pan else null
    }

    fun reset() = votes.clear()
}
