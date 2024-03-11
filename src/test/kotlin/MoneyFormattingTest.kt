import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.test.Test

class MoneyFormattingTest {
    private val units = charArrayOf('\u0000', 'K', 'M', 'B', 'T', 'Q')

    @Test
    fun onTestCompactFormat() {
        println(10000.00.compactFormat("#,##0.00", "#,##0.0##", "cs_CZ"))
    }

    private fun Double.compactFormat(pattern: String, compactPattern: String, locale: String): String {
        val (value, unit) = compactNumber(this) ?: (null to null)

        return value?.let { formatNumber(value, compactPattern, locale) + unit }
            ?: formatNumber(this, pattern, locale)
    }

    private fun compactNumber(number: Double): Pair<Double, Char>? {
        var value = number
        for (unit in units) {
            if (value < 1000) {
                return if (unit == units[0]) null
                else {
                    return Pair(value, unit)
                }
            }
            value /= 1000
        }
        throw IllegalStateException("This shouldn't happen")
    }

    private fun formatNumber(number: Double, pattern: String, locale: String, compacted: Boolean = false): String {
        val formatter = DecimalFormat().apply {
            decimalFormatSymbols = DecimalFormatSymbols.getInstance(getLocale(locale))
            roundingMode = if (compacted) {
                RoundingMode.UNNECESSARY
            } else {
                RoundingMode.HALF_UP
            }
            applyPattern(pattern)
        }
        return formatter.format(number)
    }

    private fun getLocale(localeStr: String): Locale {
        val parts = localeStr.split("-", "_")
        return when (parts.size) {
            1 -> Locale(parts[0])
            2 -> Locale(parts[0], parts[1])
            else -> Locale(parts[0], parts[1], parts[2])
        }
    }
}