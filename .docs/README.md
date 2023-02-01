# LiteEco

## Contents
- [Setup](#setup)
- [Permissions](#permission)
- [Translations](#translation)
- [Placeholders](#placeholder)
- [Contribution](#contribution-guide)

## Setup

- This is a default config.yml
  - There you can change whatever you need.
  - Example default_money is added value when account for new player was created.
````YAML
#Official settings for this plugin.
plugin:
  # Translations list of supported locales below
  # https://github.com/EncryptSL/LiteEco/blob/main/src/main/kotlin/encryptsl/cekuj/net/api/enums/TranslationKey.kt
  translation: EN_US
  economy:
    # Prefix of Currency
    prefix: €
    # Name of Currency
    name: LE
    # This amount is added to player from first connection.
    default_money: 30
  # This settings disable messages.
  disableMessages:
    g_broadcast_pay: false
    g_broadcast_withdraw: false
    g_broadcast_set: false
    target_success_pay: false
    target_success_withdraw: false
    target_success_set: false
# Settings for Database or SQLite connection.
database:
    connection:
      # Settings for JDBC_HOST example for connection to mysql: jdbc:mysql://your_host:port/name_of_database
      jdbc_host: "jdbc:sqlite:plugins/LiteEco/database.db"
      # User = username of user to connection your database.
      user: user_name
      # Pass = password of user to connection your database.
      pass: password
````

## Permission
All available permissions for commands:

**Player commands** - `lite.eco.player`
```YAML
  /money:
    Permission: lite.eco.money
  /money help: This shows commands for players
    Permission: lite.eco.help
  /money bal [username]: This shows your balance of account or another player
    Permission: lite.eco.balance
  /money top [page]: This shows richest players
    Permission: lite.eco.top
  /money pay <player> <amount>: Send your money to another player.
    Permission: lite.eco.pay
```

**Admin commands** - `lite.eco.admin`
```YAML
/eco:
  Permission: lite.eco.admin.eco
/eco help: This shows commands for admins
  Permission: lite.eco.admin.help
/eco add <player> <amount>: Add money to player.
  Permission: lite.eco.admin.add
/eco gadd <amount>: Add money to everyone.
  Permissions: lite.eco.admin.gadd
/eco set <player> <amount>: Set fixed money amount to player.
  Permission: lite.eco.admin.set
/eco gset <amount>: Set fixed money amount to everyone.
  Permissions: lite.eco.admin.gset
/eco remove <player> <amount>: Withdraw money from player.
  Permission: lite.eco.admin.remove
/eco gremove <amount>: Withdraw money from everyone.
  Permissions: lite.eco.admin.gremove
/eco lang [CS_CZ, EN_US]: This command switches translations files.
  Permission: lite.eco.admin.lang
/eco purge <argument>: This command purges data.
  Permission: lite.eco.admin.purge
/eco migration <argument>: This command migration database to file.
  Permision: lite.eco.admin.migration
/eco reload: This command reloads the config.
  Permission: lite.eco.admin.reload
```

**Other perms**
```YAML
This permission was to suggest player names. (DISABLED)
  Permission: lite.eco.suggestion.players
```

## Translation
This plugin supports translations but it must be included in the plugin.
Plugin makes use of Enums for [available locales](https://github.com/LcyDev/LiteEco/blob/main/src/main/kotlin/encryptsl/cekuj/net/api/enums/LangKey.kt).

If you want to contribute with another translation you must follow this steps.
- FileName must be in this format with the [locale code](https://www.ibm.com/docs/en/radfws/9.6.1?topic=overview-locales-code-pages-supported): `lang-ru_ru.yml`
- Translation files inside contains placeholders <example_something> please don't remove this.
- You can change colors or something what you want.

Link to files: https://github.com/EncryptSL/LiteEco/tree/main/src/main/resources/locale

## Placeholder
- `%liteeco_balance%`
  - This show normal balance output.
- `%liteeco_balance_formatted%`
   - This show fancy balance output.
- `%liteeco_top_player_<number>%`
   - This show player name on position of your set number.
- `%liteeco_top_balance_<number>%`
   - Max number is 10, this show balance of player on position of your set number.
- `%liteeco_top_formatted_<number>%`
   - Max number is 10, this show balance of player on position of your set number but fancy.
  
![hologram](https://user-images.githubusercontent.com/9441083/170329930-9e457436-fd89-4fde-ab19-0dbc843d12bd.png)

## Contribution Guide

If you want to contribute to my project you must have Intellij Idea and git.

Write this command into cmd or console and wait until project download success.

`git clone https://github.com/EncryptSL/LiteEco.git`

Please before sending PR use formatting and check if imports are optimized.

