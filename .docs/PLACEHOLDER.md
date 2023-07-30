## Placeholder
LiteEco uses placeholders in its output to dynamically display various information.

Below are the available placeholders:

- `%liteeco_balance%`
  - Displays the raw balance output for the player.


- `%liteeco_balance_compacted%`
  - Displays the balance in a compacted format, using metric prefixes to abbreviate large numbers.


- `%liteeco_balance_formatted%`
  - Displays the balance in a fancy format, making it more visually appealing.
  - (Includes currency prefix and currency name) (It may be compacted depending on a setting)


- `%liteeco_top_player_<number>%`
  - Displays the name of the player at the specified position in the richest player list.


- `%liteeco_top_balance_<number>%`
  - Displays the raw balance of the player at the specified position in the richest player list.
  - (The maximum value is 10)


- `%liteeco_top_formatted_<number>%`
  - Displays the balance in a fancy format, from the player at the specified position in the richest player list.
  - (The maximum value is 10)

![hologram](https://user-images.githubusercontent.com/9441083/170329930-9e457436-fd89-4fde-ab19-0dbc843d12bd.png)