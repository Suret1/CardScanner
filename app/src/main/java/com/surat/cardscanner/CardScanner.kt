package com.surat.cardscanner

import org.opencv.android.OpenCVLoader

object CardScanner {

    private var initialized = false

    fun init() {
        if (initialized) return
        val success = OpenCVLoader.initLocal()
        check(success) {
            "CardScanner: OpenCV inisializasiyası uğursuz oldu. " +
                    "Cihaz ABI-si dəstəklənmir: ${System.getProperty("os.arch")}"
        }
        initialized = true
    }

    fun isInitialized(): Boolean = initialized
}
