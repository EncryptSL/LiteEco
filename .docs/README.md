# LiteEco

## Contents
- [Setup](#setup)
- [Permissions](#permission)
- [Translations](#translation)
- [Placeholders](#placeholder)
- [Contribution](#contribution guide)

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
```YAML
/money: This show your currency
  Permission: lite.eco.money
/money help: This show commands
  Permission: lite.eco.help
/money t|target <player>: This show target currency
  Permission: lite.eco.target
/money top: This show richest players
  Permission: lite.eco.top
/money pay <player> <amount>: This command you send your money to another.
  Permission: lite.eco.pay
/money add <player> <amount>: This command is for admins - add money to player.
  Permission: lite.eco.add
/money set <player> <amount>: This command is for admins - set fixed amount to player.
  Permission: lite.eco.set
/money remove <player>: This command remove player money account from database.
  Permission: lite.eco.remove
/money lang [CS_CZ, EN_US]: This command switch translations files.
  Permission: lite.eco.lang
/money reload: This command reload configs.
  Permission: lite.eco.reload
```

## Translation
My plugin support's translation but must be included in plugin.
Plugin use for enabling Enums with available locales.

If you want to contribute with another translation you must follow this steps.
- FileName must be in this format: translation-RU_RU dot yml
- Translation files have inside placeholders <example_something> please no remove this.
- You can change colors or something what you want.

Link to files: https://github.com/EncryptSL/LiteEco/tree/main/src/main/resources/locale

## Placeholder
- %liteeco_balance%
  - This show normal balance output.
- %liteeco_balance_formatted%
   - This show fancy balance output.
- %liteeco_top_player_<number>% 
   - This show player name on position of your set number.
- %liteeco_top_balance_<number>%
   - Max number is 10, this show balance of player on position of your set number.
- %liteeco_top_formatted_<number>% 
   - Max number is 10, this show balance of player on position of your set number but fancy.
  
![hologram](https://user-images.githubusercontent.com/9441083/170329930-9e457436-fd89-4fde-ab19-0dbc843d12bd.png)

## Contribution Guide

If you want to contribute to my project you must have Intellij Idea and git.

Write this command into cmd or console and wait until project download success.

`git clone https://github.com/EncryptSL/LiteEco.git`

Please before sending PR use formatting and check if imports are optimized.

