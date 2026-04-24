package de.kesuaheli.twitchchatbridge;

import com.mojang.blaze3d.systems.RenderSystem;
import de.kesuaheli.twitchchatbridge.badge.BadgeSet;
import de.kesuaheli.twitchchatbridge.commands.TwitchBaseCommand;
import de.kesuaheli.twitchchatbridge.config.ModConfigFile;
import de.kesuaheli.twitchchatbridge.config.ModConfig;
import de.kesuaheli.twitchchatbridge.twitch_integration.Bot;
import de.kesuaheli.twitchchatbridge.util.Constants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.PackType;
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
    ResourceLoader.get(PackType.CLIENT_RESOURCES)
        .registerReloader(Constants.id("reload"), new TwitchChatResourceReloadListener());

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

  public static void addTwitchMessage(Component message) {
    if (Minecraft.getInstance().player == null) {
      return;
    }

    if (CONFIG.broadcast()) {
      if (Minecraft.getInstance().player != null) {
        Minecraft.getInstance().player.displayClientMessage(message, false);
        return;
      }
    }

    if (RenderSystem.isOnRenderThread()) {
      Minecraft.getInstance().gui.getChat().addMessage(message);
    } else {
      Minecraft.getInstance().executeIfPossible(() -> Minecraft.getInstance().gui.getChat().addMessage(message));
    }
  }

  public static void addNotification(MutableComponent message) {
    if (Minecraft.getInstance().player == null) {
      return;
    }

    if (RenderSystem.isOnRenderThread()) {
      Minecraft.getInstance().gui.getChat().addMessage(message.withStyle(ChatFormatting.DARK_GRAY));
    } else {
      Minecraft.getInstance().executeIfPossible(() -> Minecraft.getInstance().gui.getChat().addMessage(message.withStyle(ChatFormatting.DARK_GRAY)));
    }
  }
}
