package de.kesuaheli.twitchchatbridge.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;

public class TwitchBroadcastCommand extends LiteralArgumentBuilder<FabricClientCommandSource> {
  TwitchBroadcastCommand() {
    super("broadcast");
    then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
      .executes(this::execute)
    );
    executes(this::execute);
  }

  private int execute(CommandContext<FabricClientCommandSource> ctx) {
    boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
    TwitchChatMod.LOGGER.info("enabled is " + enabled);

    CONFIG.broadcast(enabled);
    // Also switch channels if the bot has been initialized
    if (enabled) {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.broadcast.enabled"));
    } else {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.broadcast.disabled"));
    }
    CONFIG.save();
    return 1;
  }
}
