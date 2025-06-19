package com.github.encryptsl.lite.eco.common.database

import org.junit.jupiter.api.Test

class AmountValidatorListTest {

    @Test
    fun testOrderList() {
        val rawInput = "15".trim().lowercase()
        val base = rawInput.filter { it.isDigit() || it == '.' }

        val units = listOf("k", "m", "b", "t", "q")

        val list = buildList {
            if (base.startsWith(rawInput) || rawInput.startsWith(base)) {
                add(base)
            }

            units.forEach { unit ->
                val candidate = base + unit
                if (candidate.startsWith(rawInput)) {
                    add(candidate)
                }
            }
        }

        println(list)
    }

}