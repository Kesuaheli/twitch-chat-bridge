# Twitch Chat Bridge

[![Build Twitch Chat](https://github.com/Kesuaheli/twitch-chat-bridge/actions/workflows/build.yml/badge.svg)](https://github.com/Kesuaheli/twitch-chat-bridge/actions/workflows/build.yml)
[![Release](https://github.com/Kesuaheli/twitch-chat-bridge/actions/workflows/release.yml/badge.svg)](https://modrinth.com/mod/twitch-chat-bridge)

A Fabric mod that allows you to connect your Minecraft chat to a Twitch chat and vise versa.

Chat messages from your selected Twitch channel get displayed in your Minecraft chat. Usernames in the MC chat are colored in the users selected color. Also all badges of the user are displayed before their username.

## Download

[![Download on Modrinth](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2FuhFB00mS&query=downloads&logo=modrinth&label=Download%20on%20Modrinth&labelColor=1B1E2B&suffix=%20downloads&color=1BD96A)](https://modrinth.com/mod/twitch-chat-bridge)

## Usage

1. Open the mods config using ModMenu
2. Go to the credentials tab and fill in your username
3. Get a token of your twitch account from [twitchtokengenerator.com](https://twitchtokengenerator.com/) and also fill in the token.
   
   **NEVER SHARE THIS SITE AFTER LOGGING IN. ALWAYS KEEP ALL YOUR TOKENS AND SECRETS PRIVATE.** They give access to your logged in accout.
4. Go back ingame and type in the commands:
   - `/twitch enable` to enable the connection
   - `/twitch watch CHANNEL` (replace `CHANNEL` with a channel name) to join that channel
5. You now receive messages of that channels Twitch chat
   - You can write to that channels chat by typing a `:` at the start of your message (configureable via modmenu)
   - for example writing the message `:hello guys` sends the message `hello guys` to the joined twitch channel.

---

## Other

NOTE: Even though the mod is on beta, it's fully functional! It's on beta to symbolise I'm not finished adding features.
If you encounter any bug or issue or have any suggestion, please add it as an issue
[here](https://github.com/Kesuaheli/twitch-chat-bridge/issues/new).

This Mod was forked from [pblop/twitch-chat](https://github.com/pblop/twitch-chat) and I added some further additions and bug fixes as well as still improving it.
Thanks for the Mod base.
