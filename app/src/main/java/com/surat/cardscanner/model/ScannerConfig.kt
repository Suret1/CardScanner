package com.surat.cardscanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScannerConfig(
    val showFlashButton: Boolean = true,
    val confirmFrameCount: Int = 3,
    val vibrateOnSuccess: Boolean = true,
) : Parcelable