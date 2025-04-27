package de.kesuaheli.twitchchatbridge;

import de.kesuaheli.twitchchatbridge.badge.BadgeSet;
import de.kesuaheli.twitchchatbridge.commands.TwitchBaseCommand;
import de.kesuaheli.twitchchatbridge.config.ModConfigFile;
import de.kesuaheli.twitchchatbridge.config.ModConfig;
import de.kesuaheli.twitchchatbridge.twitch_integration.Bot;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitchChatMod implements ModInitializer {
  public final static Logger LOGGER = LoggerFactory.getLogger(TwitchChatMod.class);
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
    if (CONFIG.channel().equals("") || CONFIG.credentials.oauthKey().equals("")) {
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
    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
  }

  public static void addNotification(MutableText message) {
    if (MinecraftClient.getInstance().player == null) {
      return;
    }

    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message.formatted(Formatting.DARK_GRAY));
  }
}
