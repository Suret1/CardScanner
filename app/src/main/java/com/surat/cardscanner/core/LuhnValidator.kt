package com.surat.cardscanner.core

internal object LuhnValidator {

    fun isValid(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        if (digits.length !in 13..19) return false
        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun extractPANs(rawText: String): List<String> {
        val cleaned = rawText.replace(Regex("[^0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val results = mutableListOf<String>()

        Regex("""\b(\d{4}\s?\d{4}\s?\d{4}\s?\d{4})\b""")
            .findAll(cleaned)
            .map { it.value.replace(" ", "") }
            .filter { isValid(it) }
            .forEach { results.add(it) }

        Regex("""\b(\d{4}\s?\d{6}\s?\d{5})\b""")
            .findAll(cleaned)
            .map { it.value.replace(" ", "") }
            .filter { isValid(it) }
            .forEach { results.add(it) }

        return results.distinct()
    }
}
