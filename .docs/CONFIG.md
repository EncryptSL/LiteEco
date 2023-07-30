## Setup
The configuration file (**config.yml**) is where you can customize the settings for the LiteEco plugin to suit your specific needs.

Below is the default config, make sure to modify these settings as per your server requirements.

````YAML
#Official settings for this plugin.
plugin:
  # Translations list of supported locales below
  # https://github.com/EncryptSL/LiteEco/blob/main/src/main/kotlin/encryptsl/cekuj/net/api/enums/LangKey.kt
  translation: EN_US
  economy:
    # Prefix of Currency
    prefix: €
    # Name of Currency
    name: LE
    # This amount is added to player from first connection.
    default_money: 30
    # Convert big numbers into a more compact display.
    compact_display: false
  # These settings disable messages.
  disableMessages:
    g_broadcast_pay: false
    g_broadcast_withdraw: false
    g_broadcast_set: false
    target_success_pay: false
    target_success_withdraw: false
    target_success_set: false
# Settings for Database or SQLite connection.
database:
  # MySQL example: jdbc:mysql://your_host:port/name_of_database
  connection:
    # Settings for JDBC_HOST
    jdbc_host: "jdbc:sqlite:plugins/LiteEco/database.db"
    # User = username of user to connection your database.
    user: user_name
    # Pass = password of user to connection your database.
    pass: password
````