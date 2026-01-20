package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie
import org.bstats.charts.SingleLineChart

class MetricsCollector(
    private val liteEco: LiteEco,
    private val serviceId: Int,
    private val transactions: Map<String, Int>,
) {
    private val metrics by lazy { Metrics(liteEco, serviceId) }

    internal fun setupMetrics() {
        if (liteEco.config.getBoolean("plugin.metrics", true)) {
            registerCharts()
        }
    }

    private fun registerCharts() {
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