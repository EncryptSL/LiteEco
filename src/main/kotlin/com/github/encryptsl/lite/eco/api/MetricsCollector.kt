package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie
import org.bstats.charts.SingleLineChart

class MetricsCollector(
    private val metrics: Metrics,
    private val liteEco: LiteEco,
    private val transactions: Map<String, Int>,
) {
    internal fun registerCharts() {
        metrics.addCustomChart(SingleLineChart("transactions") {
            transactions["transactions"]
        })

        metrics.addCustomChart(AdvancedPie("used_language") {
            val map = mutableMapOf<String, Int>()

            val lang = (liteEco.config.getString("plugin.translation") ?: "en_us").lowercase()
            map[lang] = 1

            return@AdvancedPie map
        })
    }
}