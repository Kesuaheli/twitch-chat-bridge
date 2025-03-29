package de.kesuaheli.twitchchatbridge.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TwitchWatchSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        if (MinecraftClient.getInstance().world != null && CONFIG.twitchWatchSuggestions()) {
            List<AbstractClientPlayerEntity> players = MinecraftClient.getInstance().world.getPlayers();

            for (AbstractClientPlayerEntity player : players) {
                builder.suggest(player.getName().getString());
            }
        }

        return builder.buildFuture();
    }
}
