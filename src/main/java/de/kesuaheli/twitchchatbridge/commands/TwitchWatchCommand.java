package de.kesuaheli.twitchchatbridge.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;

public class TwitchWatchCommand extends LiteralArgumentBuilder<FabricClientCommandSource> {
  TwitchWatchCommand() {
    super("watch");
    then(ClientCommandManager.argument("channel_name", StringArgumentType.string())
      .suggests(new TwitchWatchSuggestionProvider())
      .executes(this::execute)
    );
  }

  private int execute(CommandContext<FabricClientCommandSource> ctx) {
    String channelName = StringArgumentType.getString(ctx, "channel_name");

    CONFIG.channel(channelName);
    // Also switch channels if the bot has been initialized
    if (TwitchChatMod.bot != null) {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.watch.switching", channelName));
      TwitchChatMod.bot.joinChannel(channelName);
    } else {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.watch.connect_on_enable", channelName));
    }
    CONFIG.save();
    return 1;
  }
}
