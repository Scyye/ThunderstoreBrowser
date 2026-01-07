Updated `README.md` with a clickable table of contents and expanded credits.

```markdown
# ThunderstoreBot

[![Build Status](https://img.shields.io/github/actions/workflow/status/Scyye/thunderstorebot/maven.yml?branch=main)](https://github.com/Scyye/thunderstorebot/actions)  
[![Maven Central](https://img.shields.io/maven-central/v/dev.scyye/thunderstorebot)](https://search.maven.org/)  
[![License](https://img.shields.io/badge/license-ADD%20LICENSE-lightgrey)](LICENSE)

A Discord bot for browsing and interacting with Thunderstore content (packages, communities, profiles) with autocomplete, menus, caching and admin controls.

Repository: `thunderstorebot`  
Current branch (reference): `v1.3-readme`

Contents
- [Features](#features)
- [Commands (full list)](#commands-full-list)
- [Menus & Context commands](#menus--context-commands)
- [Configuration & persistence](#configuration--persistence)
- [Self-hosting / Development](#self-hosting--development)
- [Contributing](#contributing)
- [Credits & License](#credits--license)
- [Known issues](#known-issues)
- [Support / Contact](#support--contact)

---

# Features

- Search and inspect Thunderstore communities and packages.
- Package info & search with autocomplete and client-side caching for fast responses.
- Profile (R2ModManager) profile parsing to list mods from shared profiles.
- Log parsing to extract plugin and error info from BepInEx `LogOutput`.
- Admin utilities: toggle channel/user access, manage moderator roles, rename bot, set default community, view config.
- Suggestion DM listener: users can DM the bot to send suggestions to the configured owner.
- Menus (paginated embeds) for long result sets.
- Context menu support (message context) to run commands on selected messages.
- Owner and permission checks; ephemeral replies for privacy where applicable.
- Configurable per-guild settings persisted via BotCommons `ConfigManager`.

---

## Commands (full list)

General usage: slash command style, e.g. `/package info community:<community> uuid:<uuid>`

- `package` (group)
  - `info` — Get info about a package (params: `community`, `uuid`)
  - `search` — Search packages in a community (params: `community`, `search`, `author`, `depends`)
- `community` (group)
  - `info` — Get information about a community (param: `community`)
  - `list` — List all communities (paginated)
- `profile` (group)
  - `modlist` — Get mods from a R2ModManager profile by UUID
  - Context message command: `List Mods`
- `logparse` (group)
  - `parseinfo` — Parse a `LogOutput` file (attachment)
  - `pluginlist` — Show full plugin list from a `LogOutput` attachment
- `admin` (group)
  - `toggle-channel` — Toggle the bot for the specified channel
  - `toggle-user` — Toggle a user's access to the bot (owner-only)
  - `toggle-mod-role` — Add/remove a role as moderator role
  - `view-config` — Display the server's config
  - `rename` — Change the bot's nickname
  - `community` — Set default allowed community for the server
- Misc / general
  - `ping`, `echo` (owner only), `changelog`, `version`, `invite`, `credits`, `soup`
- Utility
  - `addinvite` — (owner only) create a 1-use invite to a guild

See inline command help in the bot for parameter details and permissions.

---

## Menus & Context commands

- Implemented with BotCommons `Menu` & `PageMenu`.
- Menus: `package-search-menu`, `changelog`, `community-list`, `plugin-list`, `modlist-menu`.
- Context message command: `List Mods` runs `modlist` on the selected message content.

---

## Configuration & persistence

Config storage uses `botcommons.config.ConfigManager`. Default per-guild config keys:
- `disabledChannels` — String[] of channel IDs where bot is disabled
- `disabledUsers` — String[] of user IDs banned from using bot
- `moderatorRoles` — String[] of role IDs treated as moderators
- `community` — default community identifier for the guild (string)

Global runtime config:
- `token` — bot token (required)
- `owner-id` — owner user id (used for owner-only commands)

Assets and runtime files:
- `thunderstorebot-assets/`
  - `logs/` — downloaded logs for parsing
  - `profiles/` — saved profile zips
  - `soup.png` — used by `soup` command

---

## Self-hosting / Development

Requirements:
- Java 17+
- Maven
- Windows (tested on Windows; asset paths use backslashes in code)

Build & run:
1. Clone and enter project: `git clone <repo-url>` then `cd thunderstorebot`
2. Configure `token` and `owner-id` for `Config` (BotCommons config)
3. Build: `mvn clean package`
4. Run: `java -jar target/thunderstorebot-<version>.jar` or run from IntelliJ IDEA

Local testing:
- Create test Discord server and invite bot with MESSAGE_CONTENT and GUILD_MEMBERS intents.
- Ensure `thunderstorebot-assets` directory is writable.

---

## Contributing

- Open an issue first for feature requests / bugs.
- Use feature branches and PRs against `main`.
- Follow existing project style and add tests under `src/test/java`.
- Commands: `src/main/java/dev/scyye/thunderstorebot/command/impl/`
- Utilities: `src/main/java/dev/scyye/thunderstorebot/utils/`
- `Main` bootstraps commands and menus.

Add a `LICENSE` (e.g. `MIT`) if you want contributors and users to have clear rights.

---

## Credits & License

Core project
- Author / lead maintainer: `Scyye` — project lead, TSJA author and primary bot developer.

Contributors & testers
- `Poppycars`
- `Anarkey`
- `Root`
- `Assist` / `Ascyst`
- `Atomic();`
- `Justin` (credited for `soup`)

Libraries / frameworks
- JDA — Java Discord API client
- BotCommons — command, menu and config utilities (used extensively)
- Thunderstore Java API (TSJA) — `dev.scyye`
- Gson — JSON serialization
- Maven — build system

Assets
- `Keyanlux` — art and visual assets used by the bot

Special thanks
- Testers and community contributors for reporting bugs and providing feedback.

License
- This repository currently does not ship a license file. Add `LICENSE` (e.g. `MIT`) to clarify usage and contribution terms.

---

## Known issues

- Server-enforced channel and user bans can have edge cases.
- Some DM and permission edge conditions may require owner intervention.
- Check issue tracker for current bug reports.

---

## Support / Contact

- Primary contact: `Scyye` (set `owner-id` in config to route suggestions)
- Suggestion DM listener forwards private messages to the configured owner automatically.

---

If you host this project, please add a `LICENSE` file, update badges to point at your repository/CI and keep `owner-id` and `token` secure.
```
