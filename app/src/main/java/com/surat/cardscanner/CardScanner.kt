package com.surat.cardscanner

object CardScanner {

    @Deprecated(
        "init() artıq tələb olunmur. OpenCV library-dən çıxarılıb.",
        level = DeprecationLevel.WARNING
    )
    fun init() = Unit

    @Deprecated(
        "isInitialized() artıq istifadə olunmur.",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith("true")
    )
    fun isInitialized(): Boolean = true
}
