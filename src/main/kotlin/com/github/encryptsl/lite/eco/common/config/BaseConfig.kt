package com.github.encryptsl.lite.eco.common.config

import de.exlll.configlib.Comment
import de.exlll.configlib.Configuration

@Configuration
class BaseConfig {
    @Comment("Official settings for this plugin.", "You can see our official wikipedia page: https://liteeco.github.io/")
    var plugin = PluginSettings()

    var economy = EconomySettings()

    var formatting = FormattingSettings()

    @Comment("These settings control message notifications.")
    var messages = MessageSettings()

    @Comment("Settings for database connection (SQLite, MySQL, MariaDB, PostgreSQL).")
    var database = DatabaseSettings()

    @Configuration
    class PluginSettings {
        @Comment(
            "List of supported locales for translations.",
            "See more at: https://github.com/EncryptSL/LiteEco/tree/main/src/main/resources/locale"
        )
        var translation = "EN_US"
        @Comment("Plugin prefix displayed in chat.")
        var prefix = "<dark_gray>[<green>Eco<dark_gray>] <dark_green>»</dark_gray>"
        @Comment("Enable or disable plugin metrics collection.")
        var metrics = true
        @Comment("Enable or disable Vault debug messages.")
        var vaultDebug = false

        @Comment("Settings for player suggestions in commands", "", "true: Suggests offline players" ,"false: Suggests only online players.")
        var offlineSuggestionPlayers = true
    }

    @Configuration
    class EconomySettings {
        @Comment(
            "Applicable since version 1.4.8 and newer.",
            "",
            "Currency Settings Guide:",
            " - currency_name: Internal name of the currency.",
            " - currency_plural_name: Name used when amount is plural (e.g., dollars).",
            " - currency_singular_name: Name used when amount is singular (e.g., dollar).",
            " - currency_format: Pattern for display. Use '<money>' as a placeholder.",
            " - starting_balance: The initial amount granted to new players.",
            " - balance_limit: The maximum allowed balance for a player.",
            " - balance_limit_check: If enabled, enforces the balance limit.",
            " - compact_display: Converts large values into compact format (e.g., 1M).",
            "",
            "The key of each currency corresponds to its table name in the database.",
            "Changing this value may result in data loss."
        )
        var currencies: Map<String, CurrencySettings> = mapOf(
            "dollars" to CurrencySettings()
        )

        @Comment(
            "Enable logging of economic activities such as adding, setting, withdrawing, and paying money.",
            "View logs using the command: /eco monolog [player]"
        )
        var monologActivity = true
    }

    @Configuration
    class CurrencySettings {
        var currencyPluralName = "dollars"
        var currencySingularName = "dollar"

        @Comment("Format for displaying currency values.")
        var currencyFormat = "$ <money>"

        @Comment("The initial amount granted to new players who do not have an existing account.")
        var startingBalance = 30

        @Comment("The maximum allowed balance for a player.")
        var balanceLimit = 1000000

        @Comment(
            "Enable or disable the balance limit check.",
            "If disabled, players can exceed the limit through paid amounts, or amounts given/set by administrators."
        )
        var balanceLimitCheck = true

        @Comment("Convert large currency values into a more compact format (e.g., 1,000,000 to 1M).")
        var compactDisplay = false

        var topBalances = TopBalances()
    }

    @Configuration
    class TopBalances {
        @Comment(
            "'filtering' determines whether the 'blacklist' should be applied to exclude specific player names",
            "from the TOP balance leaderboards. Note: Names like 'NULL', 'CONSOLE', and 'SERVER' are always filtered out internally."
        )
        var filtering = false
        @Comment(
            "A list of player names that will be EXCLUDED from the TOP leaderboards.",
            "This list is only applied if 'filtering' is set to 'true'.",
            "Names in this list are compared in a case-insensitive manner."
        )
        var blacklist = listOf("ExamplePlayerName", "TestAccount123", "Notch")
    }

    @Configuration
    class FormattingSettings {
        @Comment("The format pattern for displaying currency values.")
        var currencyPattern = "#,##0.00"
        @Comment("The format pattern for displaying compacted currency values.")
        var compactedPattern = "#,##0.0##"
        @Comment("The locale used for currency symbols and formatting.")
        var currencyLocale = "en-US"
        var placeholders = PlaceholderSettings()
    }

    @Configuration
    class PlaceholderSettings {
        @Comment("Text displayed when a name placeholder is empty.")
        var emptyName = "EMPTY"
    }

    @Configuration
    class MessageSettings {
        var global = NotificationSettings()
        var target = NotificationSettings()
    }

    @Configuration
    class NotificationSettings {
        var notifyAdd = true
        var notifyWithdraw = true
        var notifySet = true
    }

    @Configuration
    class DatabaseSettings {
        @Comment(
            "Connection URL for the database. Examples:",
            "SQLite: jdbc:sqlite:plugins/LiteEco/database.db",
            "MySQL: jdbc:mysql://your_host:port/name_of_database",
            "MariaDB: jdbc:mariadb://your_host:port/name_of_database",
            "PostgreSQL: jdbc:postgresql://your_host:port/name_of_database",
            "",
            "Database drivers:",
            "MariaDB: org.mariadb.jdbc.Driver (Note: See https://youtrack.jetbrains.com/issue/EXPOSED-170 for potential issues)",
            "MySQL: com.mysql.cj.jdbc.Driver (Do not use if you have MariaDB version 11.X or newer)",
            "PostgreSQL: org.postgresql.Driver",
            "SQLite: org.sqlite.JDBC"
        )
        var connection = ConnectionSettings()
        @Comment("Enable SQL plugin logger for debugging database-related problems.")
        var sqlPluginLogger = false
    }

    @Configuration
    class ConnectionSettings {
        @Comment("The full class name of the JDBC driver.")
        var driverClassName = "org.sqlite.JDBC"
        @Comment("The JDBC connection URL.")
        var jdbcUrl = "jdbc:sqlite:plugins/LiteEco/database.db"
        @Comment("The username for connecting to your database (MySQL/MariaDB/PostgreSQL only).")
        var username = "user_name"
        @Comment("The password for connecting to your database (MySQL/MariaDB/PostgreSQL only).")
        var password = "password"
    }
}