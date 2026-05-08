package de.kesuaheli.twitchchatbridge.pronoundb_api;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

public enum Decoration {
	NONE(0),

	CATGIRL_CHIEF(0xF49898),
	COGS(0xC3591D),
	COOKIE(0xDA9F83),
	DAYTIME(0xFFAC33),
	DF_KANIN(0xE08C73),
	DF_PLUME(0xBAD9B5),
	DONATOR_AURORA(0x59B2BA),
	DONATOR_BLOSSOM(0xF4ABBA),
	DONATOR_RIBBON(0xDD2E44),
	DONATOR_STAR(0xFDD264),
	DONATOR_STRAWBERRY(0xBE1931),
	DONATOR_WARMTH(0xF4965C),
	NIGHTTIME(0x66757F),
	PRIDE(0xA1DE93),
	PRIDE_BI(0x957DAD),
	PRIDE_LESBIAN(0xFBAB74),
	PRIDE_PAN(0xF7F48B),
	PRIDE_TRANS(0xFCB6B3),
	;

	private final int color;

	Decoration(int color) {
		this.color = color;
	}

	public static @NotNull Decoration fromJsonObject(JsonObject json) {
		if (!json.has("decoration")) return NONE;
		return Decoration.fromName(json.get("decoration").getAsString());
	}

	public static @NotNull Decoration fromName(String name) {
		for (Decoration deco : Decoration.values()) {
			if (deco.name().toLowerCase().equals(name)) return deco;
		}
		return NONE;
	}

	public @NotNull TextColor getColor() {
		return TextColor.fromRgb(this.color);
	}

	public @NotNull MutableComponent decor(@NotNull MutableComponent text) {
		if (this == NONE) return text;
		return text.withColor(this.color);
	}
}
