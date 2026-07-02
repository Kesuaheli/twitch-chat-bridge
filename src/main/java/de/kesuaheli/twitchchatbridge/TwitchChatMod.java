package de.kesuaheli.twitchchatbridge;

import com.mojang.blaze3d.systems.RenderSystem;
import de.kesuaheli.twitchchatbridge.badge.BadgeSet;
import de.kesuaheli.twitchchatbridge.commands.TwitchBaseCommand;
import de.kesuaheli.twitchchatbridge.config.ModConfigFile;
import de.kesuaheli.twitchchatbridge.config.ModConfig;
import de.kesuaheli.twitchchatbridge.twitch_integration.Bot;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitchChatMod implements ModInitializer {
  public final static Logger LOGGER = LoggerFactory.getLogger(TwitchChatMod.class);
  public static final String VERSION = FabricLoader.getInstance().getModContainer("twitchchatbridge").orElseThrow().getMetadata().getVersion().getFriendlyString();
  public static ModConfig CONFIG;
  public static Bot bot;
  public static final BadgeSet BADGES = new BadgeSet();

  @Override
  public void onInitialize() {
    var hasNewConfig= FabricLoader.getInstance().getConfigDir().resolve("twitchchatbridge/config.json5").toFile().exists();
    TwitchChatMod.CONFIG = ModConfig.createAndLoad();
    if (!hasNewConfig) ModConfigFile.loadLegacy();

    // Register commands
    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
      dispatcher.register(new TwitchBaseCommand()));

    // Register reload listener
    ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
        .registerReloadListener(new TwitchChatResourceReloadListener());

    if (CONFIG.autoConnect()) {
      autoConnect();
    }
  }

  private static void autoConnect() {
    if (CONFIG.channel().isEmpty() || CONFIG.credentials.oauthKey().isEmpty()) {
      LOGGER.info("Auto-Connect enabled, but no channel or oauth key set. Please set up your config and enable the bot manually by running \"/{} enable\".", CONFIG.command());
      return;
    }

    LOGGER.info("Auto-Connect enabled. Starting bot...");
    bot = new Bot(CONFIG.credentials.oauthKey(), CONFIG.channel());
    bot.start();
  }

  public static void addTwitchMessage(Text message) {
    if (MinecraftClient.getInstance().player == null) {
      return;
    }

    if (CONFIG.broadcast()) {
      if (MinecraftClient.getInstance().player != null) {
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return;
      }
    }

    if (RenderSystem.isOnRenderThread()) {
      MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
    } else {
      MinecraftClient.getInstance().executeSync(() -> MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message));
    }
  }

  /**
   * Shows a formatted error message in the users chat.
   *
   * @param message the translation key for the message
   * @param details the details
   */
  public static void addErrorMessage(String message, MutableText details) {
    addErrorMessage(Text.translatable(message), details);
  }

  /**
   * Shows a formatted error message in the users chat.
   *
   * @param message the message
   * @param details the details
   */
  public static void addErrorMessage(MutableText message, @Nullable MutableText details) {
    message = Text.literal("[ERROR] ")
      .append(message)
      .formatted(Formatting.RED);
    if (details != null && !details.toString().isEmpty()) {
      message.append(" ").append(details.formatted(Formatting.WHITE));
    }

    TwitchChatMod.addNotification(message);
  }

  public static void addNotification(MutableText message) {
    if (MinecraftClient.getInstance().player == null) {
      return;
    }

    if (message.getStyle().getColor() == null) {
      message.formatted(Formatting.DARK_GRAY);
    }

    if (RenderSystem.isOnRenderThread()) {
      MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
    } else {
      MinecraftClient.getInstance().executeSync(() -> MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message));
    }
  }
}
