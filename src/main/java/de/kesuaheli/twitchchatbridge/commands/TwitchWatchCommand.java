package de.kesuaheli.twitchchatbridge.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import de.kesuaheli.twitchchatbridge.config.ModConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

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

    ModConfig.getConfig().setChannel(channelName);
    // Also switch channels if the bot has been initialized
    if (TwitchChatMod.bot != null) {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.watch.switching", channelName));
      TwitchChatMod.bot.joinChannel(channelName);
    } else {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.watch.connect_on_enable", channelName));
    }
    ModConfig.getConfig().save();
    return 1;
  }
}
