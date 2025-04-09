package de.kesuaheli.twitchchatbridge.config;

import blue.endless.jankson.Comment;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import io.wispforest.owo.config.annotation.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;

@Modmenu(modId = "twitchchatbridge", uiModelId = "twitchchat:config")
@Config(wrapperName = "ModConfig", name = "twitchchatbridge/config")
public class ModConfigFile {

  @Comment("The channel name currently joined")
  @RegexConstraint("^\\w{4,25}|$")
  public String channel = "";
  @Comment("Whether to automatically enable the connection to twitch when Minecraft starts")
  public boolean autoConnect = true;
  @Comment("The chat prefix to send a message to twitch")
  @PredicateConstraint("prefixConstraintFunction")
  public String prefix = ":";
  @Comment("Whether the Twitch chat should be broadcast to the entire server")
  public boolean broadcast = false;
  @Comment("The name to use for the command (Default \"twitch\" means \"/twitch\")")
  @RestartRequired
  @RegexConstraint("[a-z][a-z0-9]*")
  public String command = "twitch";

  @SectionHeader("cosmetics")

  @Comment("The prefix to write before Twitch chat messages")
  public String broadcastPrefix = "[Twitch] ";
  @Comment("How a Twitch chat messages timestamp should be formatted (Default \"[H:mm]\")")
  public String dateFormat = "[H:mm]";
  @Comment("A list of username to ignore messages form i.e. their messages don't show up in-game")
  public List<String> ignoreList = new ArrayList<>();
  @Comment("Whether to use tab completion for the \"/twitch watch <channel>\" command")
  public boolean twitchWatchSuggestions = false;
  @Comment("Whether to always show the channels user avatar as badge")
  public boolean avatarBadge = false;

  @SectionHeader("credentials")

  @Comment("""
    Your Twitch accounts oauth token
    1. Don't show this anywhere! It's basically an access to your account
    2. Generate one on https://twitchtokengenerator.com
    """)
  @Nest
  public Credentials credentials = new Credentials();

  // @Config(wrapperName = "ModConfigCredentials", name = "twitchchatbridge/credentials.yaml")
  public static class Credentials {
    public String oauthKey = "";
  }

  public static boolean prefixConstraintFunction(String prefix) {
    return CONFIG == null || !prefix.startsWith("/"+CONFIG.command());
  }

  /**
   * Tries to load config options from the old config file and sets them in the new config object.
   * <p>
   * Since its only purpose is to load the legacy config format, this method is planned to be
   * removed in version 2.0 of the mod.
   */
  @Deprecated(since = "v0.18.0b", forRemoval = true)
  public static void loadLegacy() {
    JsonObject legacyConfig;
    try {
      var legacyPath = FabricLoader.getInstance().getConfigDir().resolve("twitchchat.json");
      legacyConfig = JsonParser.parseString(new String(Files.readAllBytes(legacyPath))).getAsJsonObject();
      //noinspection ResultOfMethodCallIgnored
      legacyPath.toFile().delete();
    } catch (IOException e) {
      // Do nothing, we have no file and thus we have to keep everything as default
      return;
    }

    if (legacyConfig.has("channel")) {
      CONFIG.channel(legacyConfig.getAsJsonPrimitive("channel").getAsString());
    }
    if (legacyConfig.has("prefix")) {
      CONFIG.prefix(legacyConfig.getAsJsonPrimitive("prefix").getAsString());
    }
    if (legacyConfig.has("oauthKey")) {
      CONFIG.credentials.oauthKey(legacyConfig.getAsJsonPrimitive("oauthKey").getAsString());
    }
    if (legacyConfig.has("dateFormat")) {
      CONFIG.dateFormat(legacyConfig.getAsJsonPrimitive("dateFormat").getAsString());
    }
    if (legacyConfig.has("ignoreList")) {
      TwitchChatMod.LOGGER.info("ignoreList: " + CONFIG.ignoreList());
      var ignoreList = new ArrayList<String>();
      for (var name : legacyConfig.getAsJsonArray("ignoreList")) {
        ignoreList.add(name.getAsString());
      }
      CONFIG.ignoreList(ignoreList);
    }
    if (legacyConfig.has("twitchWatchSuggestions")) {
      CONFIG.twitchWatchSuggestions(legacyConfig.getAsJsonPrimitive("twitchWatchSuggestions").getAsBoolean());
    }
    if (legacyConfig.has("broadcast")) {
      CONFIG.broadcast (legacyConfig.getAsJsonPrimitive("broadcast").getAsBoolean());
    }
    if (legacyConfig.has("broadcastPrefix")) {
      CONFIG.broadcastPrefix(legacyConfig.getAsJsonPrimitive("broadcastPrefix").getAsString());
    }

    CONFIG.save();
    TwitchChatMod.LOGGER.info("Loaded configuration from legacy file");
  }
}
