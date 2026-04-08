package de.kesuaheli.twitchchatbridge.badge;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import com.mojang.blaze3d.systems.RenderSystem;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BadgeFont implements GlyphProvider {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("twitchchat", "badge");
    public static final FontDescription.Resource BADGE_FONT = new FontDescription.Resource(BadgeFont.IDENTIFIER);
    public static FontSet fontStorage;
    public static final List<GlyphProvider.Conditional> FONT_FILTERS = List.of(new GlyphProvider.Conditional(new BadgeFont(), FontOption.Filter.ALWAYS_PASS));
    private static final int BADGE_SIZE = 8;

    @Override
    public void close() {
        GlyphProvider.super.close();
    }

    @Nullable
    @Override
    public UnbakedGlyph getGlyph(int codePoint) {
        Badge badge = TwitchChatMod.BADGES.get(codePoint);
        if (badge == null) {
            return GlyphProvider.super.getGlyph(codePoint);
        }

        var image = badge.image();
        int width = image.getWidth();
        int height = image.getHeight();
        float scaleFactor = (float) BADGE_SIZE / width;
        return new BitmapProvider.Glyph(
            scaleFactor,
            image,
            0, 0,
            width, height,
            BADGE_SIZE+1,
            BADGE_SIZE);
    }

    @Override
    public @NonNull IntSet getSupportedGlyphs() {
        return TwitchChatMod.BADGES.codePoints();
    }

    public static FontSet newFontStorage(TextureManager textureManager) {
        Badge.loadBadges();
        fontStorage = new FontSet(new GlyphStitcher(textureManager, IDENTIFIER));
        fontStorage.reload(FONT_FILTERS, null);
        return fontStorage;
    }

    public static void reload() {
        if (RenderSystem.isOnRenderThread()) {
            reloadFontStorage();
        } else {
            Minecraft.getInstance().executeIfPossible(BadgeFont::reloadFontStorage);
        }
    }
    private static void reloadFontStorage() {
        if (BadgeFont.fontStorage == null) {
            newFontStorage(Minecraft.getInstance().getTextureManager());
        }
        BadgeFont.fontStorage.reload(BadgeFont.FONT_FILTERS, null);
    }
}
