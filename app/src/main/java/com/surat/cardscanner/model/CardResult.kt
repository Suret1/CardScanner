package com.surat.cardscanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardResult(
    val pan: String,
    val formattedPan: String = pan.chunked(4).joinToString(" "),
    val cardBrand: CardBrand = CardBrand.fromPan(pan),
    val cardType: CardType = CardType.UNKNOWN,
) : Parcelable

enum class CardType { EMBOSSED, PRINTED, UNKNOWN }

enum class CardBrand {
    VISA, MASTERCARD, AMEX, DISCOVER, UNKNOWN;

    companion object {
        fun fromPan(pan: String): CardBrand = when {
            pan.startsWith("4")                              -> VISA
            pan.take(2).toIntOrNull() in 51..55             -> MASTERCARD
            pan.take(4).toIntOrNull() in 2221..2720         -> MASTERCARD
            pan.startsWith("34") || pan.startsWith("37")    -> AMEX
            pan.startsWith("6011") || pan.startsWith("65")  -> DISCOVER
            else                                             -> UNKNOWN
        }
    }
}
