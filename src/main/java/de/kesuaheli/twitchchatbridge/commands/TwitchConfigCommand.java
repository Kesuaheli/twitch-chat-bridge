package de.kesuaheli.twitchchatbridge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import java.util.function.Consumer;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;

public class TwitchConfigCommand extends LiteralArgumentBuilder<FabricClientCommandSource> {
  TwitchConfigCommand() {
    super("config");
    then(ClientCommandManager.argument("option", new EnumArgumentHelper<>(ConfigOption.values()))
      .executes(ctx -> ctx.getArgument("option", ConfigOption.class).execute(ctx))
    );
  }

  public enum ConfigOption implements StringIdentifiable {
    RELOAD(ctx -> {
      CONFIG.load();
      ctx.getSource().sendFeedback(Text.literal("config reloaded"));
    }),
    SAVE(ctx -> {
      CONFIG.save();
      ctx.getSource().sendFeedback(Text.literal("config saved"));
    });

    private final Consumer<CommandContext<FabricClientCommandSource>> consumer;

    ConfigOption(final Consumer<CommandContext<FabricClientCommandSource>> consumer){
      this.consumer = consumer;
    }

    /** @noinspection SameReturnValue*/
    public int execute(CommandContext<FabricClientCommandSource> ctx) {
      this.consumer.accept(ctx);
      return 1;
    }

    /**
     * {@return the unique string representation of the enum, used for serialization}
     */
    @Override
    public String asString() {
      return this.toString().toLowerCase();
    }
  }
}
