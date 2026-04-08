package de.kesuaheli.twitchchatbridge.mixin;

import de.kesuaheli.twitchchatbridge.badge.BadgeFont;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;


@Mixin(FontManager.class)
public class MixinStringRenderOutput {
    @Final
    @Shadow
    private Map<Identifier, FontSet> fontSets;
    @Final
    @Shadow
    private TextureManager textureManager;

    @Inject(method="apply", at=@At("RETURN"))
    public void afterapply(FontManager.Preparation preparation, ProfilerFiller profilerFiller, CallbackInfo ci) {
        fontSets.put(BadgeFont.IDENTIFIER, BadgeFont.newFontStorage(this.textureManager));
        TwitchChatMod.LOGGER.info("Added badge font: {}", BadgeFont.IDENTIFIER);
    }
}
