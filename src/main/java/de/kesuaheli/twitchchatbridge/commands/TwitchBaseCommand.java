package de.kesuaheli.twitchchatbridge.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class TwitchBaseCommand extends LiteralArgumentBuilder<FabricClientCommandSource> {
  public TwitchBaseCommand() {
    super("twitch");
    then(new TwitchEnableCommand());
    then(new TwitchDisableCommand());
    then(new TwitchWatchCommand());
    then(new TwitchBroadcastCommand());
    then(new TwitchConfigCommand());
    executes(ctx -> {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.base.noargs1"));
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.base.noargs2"));
      return 1;
    });
  }
}
