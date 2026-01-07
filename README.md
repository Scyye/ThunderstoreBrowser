<div align="center">

![ThunderstoreBrowser Logo](thunderstorebot-assets/TSJA%20Logo.png)

# ThunderstoreBrowser

[![License](https://img.shields.io/badge/license-MIT-red)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.3-blue)](https://github.com/Scyye/ThunderstoreBrowser/releases)
[![Discord](https://img.shields.io/badge/Discord-Join%20Server-7289DA?logo=discord&logoColor=white)](https://discord.com/invite/P8W2kBJZWq)

[![TSJA](https://img.shields.io/badge/TSJA-v1.7-orange?style=flat&logo=github)](https://github.com/Scyye/TSJA)
[![BotCommons](https://img.shields.io/badge/BotCommons-v1.12-green?style=flat&logo=github)](https://github.com/Scyye/BotCommons)

**A powerful Discord bot for browsing and interacting with Thunderstore content**

[Features](#-features) • [Installation](#-installation) • [Commands](#-commands) • [Support](#-support)

</div>

---

## 📋 Table of Contents

**For Users:**
- [About](#-about)
- [Features](#-features)
- [Installation](#-installation)
- [Commands](#-commands)
- [Configuration](#-configuration)
- [Menus & Context Commands](#-menus--context-commands)
- [Support](#-support)
- [Known Issues](#-known-issues)

**For Developers:**
- [Development](#-development)
- [Contributing](#-contributing)
- [Credits](#-credits)

---

# 👥 USER DOCUMENTATION

## 🎯 About

ThunderstoreBrowser is a Discord bot that enables seamless browsing and interaction with Thunderstore content directly from Discord. It provides package search, community information, profile parsing, log analysis, and comprehensive admin controls with caching for optimal performance.

**Artifact ID**: `ThunderstoreBrowser`  
**Group ID**: `dev.scyye`  
**Version**: 1.3

---

## ✨ Features

- 🔍 **Search and inspect** Thunderstore communities and packages
- ⚡ **Fast autocomplete** with client-side caching for instant responses
- 👤 **Profile parsing** to list mods from R2ModManager shared profiles
- 📝 **Log parsing** to extract plugin and error information from BepInEx `LogOutput`
- 🛡️ **Admin utilities**: channel/user access control, moderator role management, bot customization
- 💬 **Suggestion system**: users can DM the bot to send suggestions to the owner
- 📄 **Paginated menus** for browsing long result sets
- 🖱️ **Context menu support** for running commands on selected messages
- 🔐 **Permission checks** with ephemeral replies for privacy
- ⚙️ **Per-guild configuration** persisted via BotCommons `ConfigManager`

---

## 🚀 Installation

### Requirements

- **Java 25** or higher
- **Maven** (for building)
- **Discord Bot Token** with the following intents:
  - `MESSAGE_CONTENT`
  - `GUILD_MEMBERS`

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Scyye/ThunderstoreBrowser.git
   cd ThunderstoreBrowser
   ```

2. **Configure the bot**

   Set up your configuration with:
    - `token`: Your Discord bot token (required)
    - `owner-id`: Your Discord user ID for owner-only commands (required)

3. **Build the project**
   ```bash
   mvn clean package
   ```

4. **Run the bot**
   ```bash
   java -jar target/ThunderstoreBot.jar
   ```

   Or run directly from your IDE (e.g., IntelliJ IDEA)

5. **Verify setup**
    - Ensure the `thunderstorebot-assets` directory exists and is writable
    - Invite the bot to your test server
    - Run `/ping` to verify the bot is responding

---

## 📖 Commands

All commands use Discord's slash command system.

### Package Commands

- `/package info` — Get detailed information about a specific package
    - Parameters: `community`, `uuid`
- `/package search` — Search for packages in a community
    - Parameters: `community`, `search`, `author`, `depends`

### Community Commands

- `/community info` — Get information about a specific community
    - Parameters: `community`
- `/community list` — List all available communities (paginated)

### Profile Commands

- `/profile modlist` — Extract mods from an R2ModManager profile by UUID
- **Context command**: Right-click a message → `List Mods`

### Log Parsing

- `/logparse parseinfo` — Parse a BepInEx `LogOutput` file
    - Requires: Log file attachment
- `/logparse pluginlist` — Show full plugin list from a log file
    - Requires: Log file attachment

### Admin Commands

> **Note**: Most admin commands require moderator role or server permissions

- `/admin toggle-channel` — Enable/disable the bot in specific channels
- `/admin toggle-user` — Ban/unban users from using the bot (owner only)
- `/admin toggle-mod-role` — Add/remove roles as moderator roles
- `/admin view-config` — Display the server's current configuration
- `/admin rename` — Change the bot's nickname in the server
- `/admin community` — Set default allowed community for the server

### Utility Commands

- `/ping` — Check bot latency
- `/echo` — Echo a message (owner only)
- `/changelog` — View the bot's changelog
- `/version` — Display current bot version
- `/invite` — Get the bot's invite link
- `/credits` — View credits and attributions
- `/soup` — 🍜
- `/addinvite` — Create a 1-use invite to a guild (owner only)

---

## ⚙️ Configuration

Configuration is managed through BotCommons `ConfigManager`.

### Per-Guild Settings

- **`disabledChannels`** — Array of channel IDs where the bot is disabled
- **`disabledUsers`** — Array of user IDs banned from using the bot
- **`moderatorRoles`** — Array of role IDs with moderator privileges
- **`community`** — Default community identifier for the guild

### Global Settings

- **`token`** — Discord bot token (required)
- **`owner-id`** — Owner user ID for owner-only commands (required)

### Asset Structure

The bot uses `thunderstorebot-assets/` directory:
```
thunderstorebot-assets/
├── logs/          # Downloaded logs for parsing
├── profiles/      # Saved profile zips
├── soup.png       # Used by /soup command
├── TSJA Logo.png  # Bot logo
└── TSJA Mascott.png  # Bot mascot
```

---

## 🎨 Menus & Context Commands

Built with BotCommons `Menu` and `PageMenu` systems:

**Available Menus:**
- `package-search-menu` — Paginated package search results
- `changelog` — Bot changelog viewer
- `community-list` — Browse all communities
- `plugin-list` — View plugins from logs
- `modlist-menu` — Browse profile mod lists

**Context Commands:**
- Right-click any message → `List Mods` to parse R2ModManager profiles

---

## 💬 Support

Need help or want to provide feedback?

- 💬 **Discord Server**: [Join our support server](https://discord.com/invite/P8W2kBJZWq)
- 📧 **Direct Message**: DM the bot to send suggestions directly to the owner
- 🐛 **Bug Reports**: [GitHub Issue Tracker](https://github.com/Scyye/ThunderstoreBrowser/issues)
- 💡 **Feature Requests**: Open an issue or suggest via Discord

**Primary Contact**: Scyye

---

## 🐛 Known Issues

- Server-enforced channel and user restrictions may have edge cases
- Some DM and permission scenarios may require owner intervention
- Check the [issue tracker](https://github.com/Scyye/ThunderstoreBrowser/issues) for current bug reports

**Note**: This project is currently unlicensed. Consider adding a license (e.g., MIT, Apache 2.0) to clarify usage and contribution terms.

---

# 🛠️ DEVELOPER DOCUMENTATION

## 🔧 Development

### Project Structure

```
src/main/java/dev/scyye/thunderstorebot/
├── Main.java                          # Entry point
├── command/impl/                      # Command implementations
│   ├── PackageCommand.java
│   ├── CommunityCommand.java
│   ├── ProfileCommand.java
│   ├── LogParseCommand.java
│   ├── AdminCommands.java
│   ├── MiscCommands.java
│   ├── ContextCommands.java
│   └── AddInviteCommand.java
├── utils/                             # Utility classes
│   ├── CommandUtils.java
│   ├── SuggestionListener.java
│   └── Utils.java
└── versions/
    └── Version.java
```

### Local Testing

1. Create a test Discord server
2. Invite the bot with required intents
3. Ensure `thunderstorebot-assets` is writable
4. Configure test `token` and `owner-id`
5. Run from IDE or command line

### Key Dependencies

- **JDA 5.3.1** — Java Discord API
- **BotCommons 1.12** — Command, menu, and config framework
- **Thunderstore Java API (TSJA) 1.7** — Thunderstore API client
- **Gson 2.10.1** — JSON serialization
- **Maven** — Build system

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Open an issue first** to discuss features or bugs
2. **Fork the repository** and create a feature branch
3. **Follow existing code style** and project conventions
4. **Add tests** in `src/test/java` (if applicable)
5. **Submit a PR** against the `main` branch

### Code Locations

- Commands: `src/main/java/dev/scyye/thunderstorebot/command/impl/`
- Utils: `src/main/java/dev/scyye/thunderstorebot/utils/`
- Bootstrap: `Main.java`

---

## 🎖️ Credits

### Core Team

- **Scyye** — Project lead, TSJA author, and primary bot developer

### Contributors & Testers

- **Poppycars** — Testing and feedback
- **Anarkey** — Testing and feedback
- **Root** — Testing and feedback
- **Assist / Ascyst** — Testing and feedback
- **Atomic();** — Testing and feedback
- **Justin** — Soup command credit

### Assets

- **Keyanlux** — Logo, mascot, and visual assets

### Libraries & Frameworks

- [JDA](https://github.com/discord-jda/JDA) — Java Discord API
- [BotCommons](https://github.com/Scyye/BotCommons) — Command & menu framework
- [Thunderstore Java API (TSJA)](https://github.com/Scyye/thunderstoreapi) — Thunderstore API wrapper
- [Gson](https://github.com/google/gson) — JSON library
- Maven — Build automation

### Special Thanks

To all testers and community contributors for bug reports, suggestions, and feedback!

---

<div align="center">

![ThunderstoreBrowser Mascot](thunderstorebot-assets/TSJA%20Mascott.png)

Made with ❤️ by Scyye

</div>
