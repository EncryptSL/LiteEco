<div align=center>

## LiteEco - Setup

[![Banner]](https://github.com/EncryptSL/LiteEco)

###  Customize Your Economy Plugin with Ease
</div>

---

Welcome to the LiteEco setup! The configuration file (`config.yml`) is your gateway to customizing the LiteEco plugin according to your server's unique requirements. 

Please ensure to modify these settings as per your server requirements to ensure smooth operation of the LiteEco plugin.

## Plugin Settings

This section contains essential configurations for the LiteEco plugin.

These settings you to define specific settings that govern the overall behavior and features.

```YAML
#Official settings for this plugin.
plugin:
  # Translations list of supported locales below
  # https://github.com/EncryptSL/LiteEco/blob/main/src/main/kotlin/encryptsl/cekuj/net/api/enums/LangKey.kt
  translation: EN_US
```

## Economy Settings

This section allows you to customize various aspects of the in-game currency system used by LiteEco.

These settings help you define the appearance and behavior of the virtual currency within your server.

```YAML
economy:
  # Prefix of Currency
  currency_prefix: €
  # Name of Currency
  currency_name: LE
  # This amount is granted to players who don't have an existing account in the database.
  starting_balance: 30
  # Convert large currency values into a more compact format.
  compact_display: false
```

## Message Settings

These settings control whether certain messages will be displayed or broadcasted to players.

You can enable or disable each message type using these options:

```YAML
# These settings disable messages.
disable_messages:
  global_broadcast_pay: false
  global_broadcast_withdraw: false
  global_broadcast_set: false
  target_success_pay: false
  target_success_withdraw: false
  target_success_set: false
```

## Database Settings

These settings are used to configure the database or SQLite connection for storing LiteEco data.

You can choose to use either MySQL or SQLite by providing the appropriate connection details.

```YAML
# Settings for Database or SQLite connection.
database:
  # For SQLite, the path to the SQLite database file: jdbc:sqlite:plugins/LiteEco/database.db
  # For MySQL, the JDBC connection URL in the format: jdbc:mysql://your_host:port/name_of_database
  connection:
    # Settings for JDBC_HOST
    jdbc_url: "jdbc:sqlite:plugins/LiteEco/database.db"
    # The username of the user to connect to your database (MySQL only).
    username: user_name
    # The password of the user to connect to your database (MySQL only).
    password: password
```

[Banner]: https://i.ibb.co/gvpv3CX/LiteEco.jpg