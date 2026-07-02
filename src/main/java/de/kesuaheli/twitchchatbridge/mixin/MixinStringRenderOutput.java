package de.kesuaheli.twitchchatbridge.mixin;

import de.kesuaheli.twitchchatbridge.badge.BadgeFont;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Mixin(FontManager.class)
public class MixinStringRenderOutput {
    @Final
    @Shadow
    private Map<Identifier, FontStorage> fontStorages;
    @Final
    @Shadow
    private TextureManager textureManager;

    @Inject(method="reload(Lnet/minecraft/resource/ResourceReloader$Synchronizer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at=@At("RETURN"))
    public void afterReload(ResourceReloader.Synchronizer synchronizer, ResourceManager resourceManager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> ci) {
        ci.getReturnValue().thenRun(() -> {
            fontStorages.put(BadgeFont.IDENTIFIER, BadgeFont.newFontStorage(this.textureManager));
			TwitchChatMod.LOGGER.info("Added badge font: {}", BadgeFont.IDENTIFIER);
        });
    }
}
