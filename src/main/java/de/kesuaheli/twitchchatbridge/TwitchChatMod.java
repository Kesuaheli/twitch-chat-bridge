package de.kesuaheli.twitchchatbridge;

import de.kesuaheli.twitchchatbridge.badge.BadgeSet;
import de.kesuaheli.twitchchatbridge.config.ModConfig;
import de.kesuaheli.twitchchatbridge.commands.TwitchBaseCommand;
import de.kesuaheli.twitchchatbridge.twitch_integration.Bot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitchChatMod implements ModInitializer {
  public final static Logger LOGGER = LoggerFactory.getLogger(TwitchChatMod.class);
  public static Bot bot;
  public static final BadgeSet BADGES = new BadgeSet();

  @Override
  public void onInitialize() {
    ModConfig.getConfig().load();

    // Register commands
    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
      dispatcher.register(new TwitchBaseCommand()));

    // Register reload listener
    ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
        .registerReloadListener(new TwitchChatResourceReloadListener());
  }

  public static void addTwitchMessage(Text message) {

    if (ModConfig.getConfig().isBroadcastEnabled()) {
      if (MinecraftClient.getInstance().player != null) {
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return;
      }
    }
    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
  }

  public static void addNotification(MutableText message) {
    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message.formatted(Formatting.DARK_GRAY));
  }
}
