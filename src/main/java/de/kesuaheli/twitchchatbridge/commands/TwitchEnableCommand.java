package de.kesuaheli.twitchchatbridge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import de.kesuaheli.twitchchatbridge.twitch_integration.Bot;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;

public class TwitchEnableCommand extends LiteralArgumentBuilder<FabricClientCommandSource> {
  TwitchEnableCommand() {
    super("enable");
    executes(this::execute);
  }

  private int execute(CommandContext<FabricClientCommandSource> ctx) {
    if (TwitchChatMod.bot != null && TwitchChatMod.bot.isConnected()) {
      ctx.getSource().sendFeedback(Component.translatable("text.twitchchat.command.enable.already_enabled"));
      return 0;
    }

    if (CONFIG.credentials.oauthKey().isEmpty()) {
      ctx.getSource().sendFeedback(Component.translatable("text.twitchchat.command.enable.set_config"));
      return -1;
    }

    if (CONFIG.channel().isEmpty()) {
      ctx.getSource().sendFeedback(Component.translatable("text.twitchchat.command.enable.select_channel"));
    }

    TwitchChatMod.bot = new Bot(CONFIG.credentials.oauthKey(), CONFIG.channel());
    TwitchChatMod.bot.start();
    ctx.getSource().sendFeedback(Component.translatable("text.twitchchat.command.enable.connecting").withStyle(ChatFormatting.DARK_GRAY));
    return 1;
  }
}
