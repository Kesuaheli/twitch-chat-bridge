package de.kesuaheli.twitchchatbridge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TwitchDisableCommand extends LiteralArgumentBuilder<FabricClientCommandSource> {
  TwitchDisableCommand() {
    super("disable");
    executes(this::execute);
  }

  private int execute(CommandContext<FabricClientCommandSource> ctx) {
    if (TwitchChatMod.bot == null || !TwitchChatMod.bot.isConnected()) {
      ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.disable.already_disabled"));
      return 0;
    }

    TwitchChatMod.bot.stop();
    ctx.getSource().sendFeedback(Text.translatable("text.twitchchat.command.disable.disabled").formatted(Formatting.DARK_GRAY));
    return 1;
  }
}
