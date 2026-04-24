package de.kesuaheli.twitchchatbridge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;

public class TwitchConfigCommand extends LiteralArgumentBuilder<FabricClientCommandSource> {
  TwitchConfigCommand() {
    super("config");
    then(ClientCommands.argument("option", new EnumArgumentHelper<>(ConfigOption.values()))
      .executes(ctx -> ctx.getArgument("option", ConfigOption.class).execute(ctx))
    );
  }

  public enum ConfigOption implements StringRepresentable {
    RELOAD(ctx -> {
      CONFIG.load();
      ctx.getSource().sendFeedback(Component.literal("config reloaded"));
    }),
    SAVE(ctx -> {
      CONFIG.save();
      ctx.getSource().sendFeedback(Component.literal("config saved"));
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
    public @NonNull String getSerializedName() {
      return this.toString().toLowerCase();
    }
  }
}
