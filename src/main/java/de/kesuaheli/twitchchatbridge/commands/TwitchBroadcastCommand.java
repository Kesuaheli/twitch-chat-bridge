package de.kesuaheli.twitchchatbridge.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kesuaheli.twitchchatbridge.config.ModConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class TwitchBroadcastCommand extends LiteralArgumentBuilder<FabricClientCommandSource> {
  TwitchBroadcastCommand() {
    super("broadcast");
    then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
      .executes(this::execute)
    );
  }

  private int execute(CommandContext<FabricClientCommandSource> ctx) {
    boolean enabled = BoolArgumentType.getBool(ctx, "enabled");

    ModConfig.getConfig().setBroadcastEnabled(enabled);
    // Also switch channels if the bot has been initialized
    if (enabled) {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.broadcast.enabled"));
    } else {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.broadcast.disabled"));
    }
    ModConfig.getConfig().save();
    return 1;
  }
}
