<div style="text-align: center;">

## LiteEco - Permissions

![banner](https://user-images.githubusercontent.com/9441083/215228544-29c3bfa3-f67f-4f9d-8510-bde3f133018e.jpg)

</div>

---

LiteEco provides various permissions for player and admin commands to control access to specific features.

You can use permission groups to give access to all children permissions.

Below are the available permissions.

### Player commands - `lite.eco.player`

```YAML
# Main player command for LiteEco.
/money:
  Permission: lite.eco.money

# Shows commands for players.
/money help:
  Permission: lite.eco.help

# Check your account balance or the balance of another player.
/money bal [player]:
  Permission: lite.eco.balance

# Shows the richest players.
/money top [page]:
  Permission: lite.eco.top

# Send your money to another player.
/money pay <player> <amount>:
  Permission: lite.eco.pay
```

### Admin commands : `lite.eco.admin`

```YAML
# Main admin command for LiteEco.
/eco:
  Permission: lite.eco.admin.eco

# Shows available commands for admins.
/eco help:
  Permission: lite.eco.admin.help

# Add money to a player.
/eco add <player> <amount>:
  Permission: lite.eco.admin.add

# Add money to everyone.
/eco gadd <amount>:
  Permission: lite.eco.admin.gadd

# Set a fixed money amount for a player.
/eco set <player> <amount>:
  Permission: lite.eco.admin.set

# Set a fixed money amount for everyone.
/eco gset <amount>:
  Permission: lite.eco.admin.gset

# Withdraw money from a player.
/eco remove <player> <amount>:
  Permission: lite.eco.admin.remove

# Withdraw money from everyone.
/eco gremove <amount>:
  Permission: lite.eco.admin.gremove

# Switch the translation language.
/eco lang [LANG_KEY]:
  Permission: lite.eco.admin.lang

# Purge data that meet certain requirements.
/eco purge <argument>:
  Permission: lite.eco.admin.purge

# Migrate the database between SQL or CSV.
/eco migration <argument>:
  Permission: lite.eco.admin.migration

# Reload the configuration.
/eco reload:
  Permission: lite.eco.admin.reload
```
