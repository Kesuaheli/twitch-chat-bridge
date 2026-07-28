package de.kesuaheli.twitchchatbridge.config;

import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.ListGroup;
import dev.isxander.yacl3.config.v2.api.autogen.StringField;
import dev.isxander.yacl3.config.v2.api.autogen.TickBox;
import dev.isxander.yacl3.config.v2.api.autogen.OptionAccess;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.quiltmc.parsers.json.JsonReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;

public class Config {

  private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("twitchchatbridge/config.json5");

  static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
      .id(Identifier.fromNamespaceAndPath("twitchchat", "config"))
      .serializer(config -> GsonConfigSerializerBuilder.create(config)
          .setPath(PATH)
          .setJson5(true)
          .build())
      .build();


  // @RegexConstraint("^\\w{4,25}|$")
  @SerialEntry(comment = "The channel name currently joined")
  @AutoGen(category = "general")
  @StringField
  public String channel = "";

  @SerialEntry(comment = "Whether to automatically enable the connection to twitch when Minecraft starts")
  @AutoGen(category = "general")
  @TickBox
  public boolean autoConnect = true;

  // @PredicateConstraint("prefixConstraintFunction")
  @SerialEntry(comment = "The chat prefix to send a message to twitch")
  @AutoGen(category = "general")
  @StringField
  public String prefix = ":";

  // @RestartRequired
  // @RegexConstraint("[a-z][a-z0-9]*")
  @SerialEntry(comment = "The name to use for the command (Default \"twitch\" means \"/twitch\")")
  @AutoGen(category = "general")
  @StringField
  public String command = "twitch";

  @SerialEntry(comment = "Whether to only send messages to twitch and disable receiving messages from twitch")
  @AutoGen(category = "general", group = "behaviour")
  @TickBox
  public boolean printMessagesInChat = true;

  @SerialEntry(comment = "Whether the Twitch chat should be broadcast to the entire server")
  @AutoGen(category = "general", group = "behaviour")
  @TickBox
  public boolean broadcast = false;

  @SerialEntry(comment = "A list of username to ignore messages form i.e. their messages don't show up in-game")
  @AutoGen(category = "cosmetics")
  @ListGroup(controllerFactory = CopyStringFieldImpl.class, valueFactory = CopyStringFieldImpl.class)
  public List<String> ignoreList = new ArrayList<>();

  @SerialEntry(comment = "Whether to use tab completion for the \"/twitch watch <channel>\" command")
  @AutoGen(category = "cosmetics")
  @TickBox
  public boolean twitchWatchSuggestions = false;

  @SerialEntry(comment = "How a Twitch chat messages timestamp should be formatted (Default \"[H:mm]\")")
  @AutoGen(category = "cosmetics", group = "formatting")
  @StringField
  public String dateFormat = "[H:mm]";

  @SerialEntry(comment = "The prefix to write before Twitch chat messages")
  @AutoGen(category = "cosmetics", group = "formatting")
  @StringField
  public String broadcastPrefix = "[Twitch] ";

  @SerialEntry(comment = "Whether to show the users pronouns inline or only when hovering their username")
  @AutoGen(category = "cosmetics", group = "formatting")
  @TickBox
  public boolean showPronounsInline = true;

  @SerialEntry(comment = "Whether to always show the channels user avatar as badge")
  @AutoGen(category = "cosmetics", group = "formatting")
  @TickBox
  public boolean avatarBadge = false;

  @SerialEntry(comment = """
    Your Twitch accounts oauth token
    1. Don't show this anywhere! It's basically an access to your account
    2. Generate one on https://twitchtokengenerator.com
  """)
  @AutoGen(category = "credentials")
  @StringField
  public String oauthKey = "";

  public static void save() {
    HANDLER.save();
    TwitchChatMod.LOGGER.info("config saved!");
  }

  public static void load() {
    String oldConfigString;
    try {
      oldConfigString = Files.readString(PATH);
    } catch (IOException ignored) {
      oldConfigString = "";
    }

    HANDLER.load();
    CONFIG = HANDLER.instance();

    if (oldConfigString.isBlank()) return;

    try {
      if (CONFIG.migrateLegacy(oldConfigString)) save();
    } catch (IOException ignored) {
    }
  }

  public static Config instance() {
    return HANDLER.instance();
  }

  private boolean migrateLegacy(String oldConfig) throws IOException {
    final JsonReader jsonReader = JsonReader.json5(oldConfig);
    try {
      return migrateLegacy(jsonReader);
    } catch (IOException e) {
      jsonReader.close();
      throw e;
    }
  }

  private boolean migrateLegacy(JsonReader jsonReader) throws IOException {
    boolean changedAny = false;
    jsonReader.beginObject();
    while (jsonReader.hasNext()) {
      final String jsonKey = jsonReader.nextName();
      if (this.oauthKey.equals(HANDLER.defaults().oauthKey) && jsonKey.equals("credentials")) {
        changedAny |= this.migrateCredentials(jsonReader);
        continue;
      }
      jsonReader.skipValue();
    }
    jsonReader.close();
    return changedAny;
  }

  private boolean migrateCredentials(JsonReader jsonReader) throws IOException {
    boolean changedAny = false;
    jsonReader.beginObject();
    while (jsonReader.hasNext()) {
      final String jsonKey = jsonReader.nextName();
      if (!jsonKey.equals("oauthKey")) {
        jsonReader.skipValue();
        continue;
      }
      String jsonVal = jsonReader.nextString();
      if (jsonVal.equals(HANDLER.defaults().oauthKey)) continue;
      this.oauthKey = jsonVal;
      changedAny = true;
    }
    return changedAny;
  }

  public static class CopyStringFieldImpl implements ListGroup.ControllerFactory<String>, ListGroup.ValueFactory<String> {

    @Override
    public ControllerBuilder<String> createController(ListGroup annotation, ConfigField<List<String>> field, OptionAccess storage, Option<String> option) {
      return StringControllerBuilder.create(option);
    }

    @Override
    public String provideNewValue() {
      return "";
    }
  }
}
