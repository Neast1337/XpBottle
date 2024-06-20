### XP Bottle Plugin Documentation

Welcome to the XP Bottle plugin documentation. This guide provides detailed instructions on how to install, configure, and use the XP Bottle plugin on your Minecraft server.

#### Installation

1. **Download the Plugin:**
   - Clone the XP Bottle repository from GitHub or download the latest release from the Releases section.

2. **Install the Plugin:**
   - Place the downloaded XP Bottle plugin JAR file into the `plugins` folder of your Spigot server.

3. **Restart or Reload the Server:**
   - Restart or reload your Spigot server to enable the XP Bottle plugin.

#### Commands

- **/xpbottle** - Converts experience points into XP bottles.
  - Usage: `/xpbottle`
  - Cooldown: 15 minutes per player.
  - Requirements: Player must have at least 1 XP level.

- **/xpbottle reload** - Reloads the plugin configuration.
  - Usage: `/xpbottle reload`
  - Permission: `bottlexp.reload`

#### Events

- **PlayerInteractEvent**
  - Players can retrieve XP levels stored in bottles by interacting with them.

#### Configuration

The XP Bottle plugin utilizes a YAML configuration file (`config.yml`) located in the plugin's folder. Here are the configurable options:

```yaml
prefix: "&7[&bXPBottle&7] " # Prefix displayed before plugin messages

messages:
    reload_success: "Configuration reloaded successfully."  # Message displayed when the configuration is successfully reloaded.
    no_permission_reload: "You do not have permission to reload the configuration."  # Message displayed when a user tries to reload the configuration without permission.
    convert_success: "Your experience has been converted into an XP bottle."  # Message displayed when a player successfully converts their experience into an XP bottle.
    not_enough_xp: "You do not have enough XP to convert."  # Message displayed when a player does not have enough XP to perform a conversion.
    player_only: "This command can only be used by a player."  # Message displayed when a non-player entity tries to use a command restricted to players.
    xp_recovered: "You have recovered {levels} levels of XP!"  # Message displayed when a player successfully recovers XP levels.
    cooldown_message: "Please wait {time} before using this command again."  # Message displayed when a player tries to use a command that is on cooldown.

time_units:
    years: "years" # Display format for years in cooldown time
    months: "months" # Display format for months in cooldown time
    weeks: "weeks" # Display format for weeks in cooldown time
    days: "days" # Display format for days in cooldown time
    hours: "hours" # Display format for hours in cooldown time
    minutes: "minutes" # Display format for minutes in cooldown time
    seconds: "seconds" # Display format for seconds in cooldown time


cooldown_seconds: 900

######### WARNING! if you change this after creating xp bottles then they will be deactivated #########
item:
    type: "PAPER"
    display_name: "&bXP Bottle"
    custom_model_data: 0
######### WARNING! if you change this after creating xp bottles then they will be deactivated #########
```

#### Compatibility

XP Bottle plugin is compatible with the following plugins:

- **ItemsAdder and Oraxen:** Integrates custom items as XP bottles.

#### Data Management

- Uses a HashMap to track cooldowns and manage XP transactions securely.

#### Technical Details

- **JavaPlugin:** Built on JavaPlugin for seamless integration with SpigotMC.
- **CommandExecutor:** Handles commands `/xpbottle` and `/xpbottle reload`.
- **Listener:** Listens to player interactions and XP retrieval events.

#### Usage

Players can use XP bottles to conveniently store and retrieve their accumulated experience points. This plugin offers a streamlined way to manage XP within your Minecraft server, enhancing gameplay mechanics.

#### Contributing

We welcome contributions to the XP Bottle plugin! To contribute:

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/YourFeature`).
3. Commit your changes (`git commit -am 'Add some feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Submit a pull request.

#### Support

For any issues, suggestions, or feedback, please [open an issue](https://github.com/Neast1337/XpBottle/issues) on GitHub.

---

This documentation is tailored for GitHub and provides comprehensive information on installation, commands, events, configuration, compatibility, data management, technical details, usage instructions, contributing guidelines, and support links. Adjust the placeholders (`yourusername`, `YourFeature`, etc.) with your actual GitHub username and feature branch name as necessary.
