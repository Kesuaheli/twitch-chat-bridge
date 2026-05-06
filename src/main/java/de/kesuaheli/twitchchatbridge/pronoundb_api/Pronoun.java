package de.kesuaheli.twitchchatbridge.pronoundb_api;

import org.jetbrains.annotations.NotNull;

public enum Pronoun {
	HE,
	IT,
	SHE,
	THEY,

	ANY,
	ASK,
	AVOID,
	OTHER,
	;

	public static Pronoun fromString(@NotNull String pronoun, @NotNull Locale locale) {
		for (Pronoun actualPronoun : Pronoun.values()) {
			if (actualPronoun.Short(locale).equals(pronoun)) return actualPronoun;
		}
		return Pronoun.OTHER;
	}

	public String Normal(@NotNull Locale locale) {
		return switch (locale) {
			case Locale.EN -> switch (this) {
				case HE -> "he/him";
				case IT -> "it/its";
				case SHE -> "she/her";
				case THEY -> "they/them";
				case ANY -> "Any pronouns";
				case ASK -> "Ask me my pronouns";
				case AVOID -> "Avoid pronouns, use my name";
				case OTHER -> "Other pronouns";
			};
		};
	}

	public @NotNull String Short(@NotNull Locale locale) {
		return switch (locale) {
			case Locale.EN -> switch (this) {
				case HE -> "he";
				case IT -> "it";
				case SHE -> "she";
				case THEY -> "they";
				case ANY -> "any";
				case ASK -> "ask";
				case AVOID -> "avoid";
				case OTHER -> "other";
			};
		};
	}

	@NotNull String Quote(@NotNull Locale locale) {
		if (this.isMeta()) return this.Normal(locale);
		return "\""+this.Normal(locale)+"\"";
	}

	public boolean isMeta() {
		return this == ANY || this == ASK || this == AVOID || this == OTHER;
	}
}
