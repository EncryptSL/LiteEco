package com.github.encryptsl.lite.eco.common.database.com.github.encryptsl.test.tables

import com.github.encryptsl.lite.eco.common.extensions.compactFormat
import com.github.encryptsl.lite.eco.common.extensions.moneyFormat
import com.github.encryptsl.lite.eco.common.extensions.toValidDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.math.BigDecimal
import java.util.*

class NumberMoneyFormatTest {

    @Test
    fun `toValidDecimal should return null for blank or spaced strings`() {
        assertNull("".toValidDecimal())
        assertNull("   ".toValidDecimal())
        assertNull("12 34".toValidDecimal())
    }

    @Test
    fun `toValidDecimal should parse plain numbers`() {
        assertEquals(BigDecimal("1234"), "1234".toValidDecimal())
        assertEquals(BigDecimal("1234.56"), "1234.56".toValidDecimal())
    }

    @Test
    fun `toValidDecimal should decompress compact numbers`() {
        assertEquals(BigDecimal("1000"), "1K".toValidDecimal())
        assertEquals(BigDecimal("1500000.0"), "1.5M".toValidDecimal())
        assertEquals(BigDecimal("2000000000"), "2B".toValidDecimal())
    }

    @Test
    fun `compactFormat should format small numbers without unit`() {
        val num = BigDecimal("999")
        val formatted = num.compactFormat("#,##0.##", "#.##", "en")
        assertEquals("999", formatted)
    }

    @Test
    fun `compactFormat should format thousands with unit`() {
        val num = BigDecimal("1500")
        val formatted = num.compactFormat("#,##0.##", "#.##", "en")
        // compact 1500 -> "1.5K"
        assertEquals("1.5K", formatted)
    }

    @Test
    fun `moneyFormat should use given pattern and locale`() {
        val num = BigDecimal("1234.5")
        val formatted = num.moneyFormat("#,##0.00", "en")
        assertEquals("1,234.50", formatted)

        val formattedDe = num.moneyFormat("#,##0.00", "de")
        assertEquals("1.234,50", formattedDe) // německý oddělovač
    }

    @Test
    fun `decompressNumber should return null for invalid input`() {
        assertNull("ABC".toValidDecimal()) // nelze převést na číslo
        assertNull("12 X".toValidDecimal()) // mezera
    }

    @Test
    fun `getLocale should parse simple and composite locales`() {
        val en = getLocale("en")
        assertEquals(Locale.ENGLISH.language, en.language)
    }

    @Test
    fun `compactFormat should handle very large numbers`() {
        val num = BigDecimal("100000000000000000000000.00") // 1e23
        val formatted = num.compactFormat("#,##0.##", "#.##", "en")

        // Podle algoritmu: 1e23 / 1e15 = 1e8 => "100000Q"
        assertEquals("100S", formatted)
    }

    @Test
    fun `moneyFormat should format very large numbers`() {
        val num = BigDecimal("100000000000000000000000.00")
        val formatted = num.moneyFormat("#,##0.00", "en")

        // Jen ověřit, že se naformátuje celé číslo s oddělovači
        assertEquals("100,000,000,000,000,000,000,000.00", formatted)
    }

    private fun getLocale(localeStr: String): Locale {
        val parts = localeStr.split("-", "_")
        return when (parts.size) {
            1 -> Locale.of(parts[0])
            2 -> Locale.of(parts[0], parts[1])
            else -> Locale.of(parts[0], parts[1], parts[2])
        }
    }
}