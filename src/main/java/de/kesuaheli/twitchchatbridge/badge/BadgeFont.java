package de.kesuaheli.twitchchatbridge.badge;

import com.mojang.blaze3d.systems.RenderSystem;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BadgeFont implements Font {
    public static final Identifier IDENTIFIER = Identifier.of("twitchchat", "badge");
    public static FontStorage fontStorage;
    public static final List<Font.FontFilterPair> FONT_FILTERS = List.of(new Font.FontFilterPair(new BadgeFont(), FontFilterType.FilterMap.NO_FILTER));
    private static final int BADGE_SIZE = 8;

    @Override
    public void close() {
        Font.super.close();
    }

    @Nullable
    @Override
    public Glyph getGlyph(int codePoint) {
        Badge badge = TwitchChatMod.BADGES.get(codePoint);
        if (badge == null) {
            return Font.super.getGlyph(codePoint);
        }

        var image = badge.image();
        int width = image.getWidth();
        int height = image.getHeight();
        float scaleFactor = (float) BADGE_SIZE / width;
        return new BitmapFont.BitmapFontGlyph(
            scaleFactor,
            image,
            0, 0,
            width, height,
            BADGE_SIZE+1,
            BADGE_SIZE);
    }

    @Override
    public IntSet getProvidedGlyphs() {
        return TwitchChatMod.BADGES.codePoints();
    }

    public static FontStorage newFontStorage(TextureManager textureManager) {
        Badge.loadBadges();
        fontStorage = new FontStorage(textureManager, IDENTIFIER);
        fontStorage.setFonts(FONT_FILTERS, null);
        return fontStorage;
    }

    public static void reload() {
        if (RenderSystem.isOnRenderThread()) {
            reloadFontStorage();
        } else {
            MinecraftClient.getInstance().executeSync(BadgeFont::reloadFontStorage);
        }
    }
    private static void reloadFontStorage() {
        if (BadgeFont.fontStorage == null) {
            newFontStorage(MinecraftClient.getInstance().getTextureManager());
        }
        BadgeFont.fontStorage.setFonts(BadgeFont.FONT_FILTERS, null);
    }
}
