package eu.pabl.twitchchat.mixin;

import eu.pabl.twitchchat.TwitchChatMod;
import eu.pabl.twitchchat.config.ModConfig;
import eu.pabl.twitchchat.twitch_integration.FormatMessage;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatMixin {
  @Inject(at = @At("HEAD"), method = "sendMessage", cancellable = true)
  private void sendMessage(String message, boolean addToHistory, CallbackInfo info) {
    ModConfig config = ModConfig.getConfig();
    String prefix = config.getPrefix();

    // Allow users to write /twitch commands (such as disabling and enabling the mod) when their prefix is "".
    if (!message.startsWith(prefix) ||
        prefix.equals("") && message.startsWith("/twitch")
    ) {
      return;
    }

    // from now on we know the message is supposed to be a twitch message, so cancel the normal sendMessage method
    info.cancel();

    if (TwitchChatMod.bot == null || !TwitchChatMod.bot.isConnected()) {
      TwitchChatMod.addNotification(Text.translatable("text.twitchchat.chat.integration_disabled"));
      return;
    }
    message = message.replaceFirst("^"+prefix, "");

    TwitchChatMod.bot.sendMessage(message); // Send the message to the Twitch IRC Chat

    final String ACTION_MESSAGE_PREFIX = "/me ";
    boolean isActionMessage = message.startsWith(ACTION_MESSAGE_PREFIX);
    if (isActionMessage) message = message.replaceFirst("^"+ACTION_MESSAGE_PREFIX, "");

    FormatMessage.formatAndSend(message, isActionMessage);
  }
}
