package de.kesuaheli.twitchchatbridge.pronoundb_api;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Locale {
	EN("en"),
	;

	private final String jsonName;

	Locale(String jsonName) {
		this.jsonName = jsonName;
	}

	private static final Map<String, Locale> JSON_TO_LOCALE;

	static {
		HashMap<String, Locale> map = new HashMap<>();
		for (Locale locale : Locale.values()) {
			map.put(locale.jsonName, locale);
		}
		JSON_TO_LOCALE = Collections.unmodifiableMap(map);
	}

	public static @NotNull Locale fromJSONName(String jsonName) {
		return JSON_TO_LOCALE.getOrDefault(jsonName, Locale.EN);
	}

	public @NotNull String formatLong(@NotNull List<Pronoun> pronouns) {
		return switch (this) {
			case EN -> {
				if (pronouns.getFirst() == Pronoun.ASK) yield "Prefers people to ask for their pronouns.";
				if (pronouns.getFirst() == Pronoun.AVOID) yield "Wants to avoid pronouns.";
				if (pronouns.getFirst() == Pronoun.OTHER) yield "Goes by pronouns not available on PronounDB.";

				String res = "Goes by ";
				for (int i = 0; i < pronouns.size(); i++) {
					if (pronouns.get(i) == Pronoun.ASK) yield res+" pronouns. You may also ask this person for additional info.";
					if (pronouns.get(i) == Pronoun.OTHER) yield res+" pronouns. This person also goes by pronouns not available on PronounDB.";
					if (i > 0) {
						res += ", ";
						if (i == pronouns.size()-1) res += "or ";
					}
					res += pronouns.get(i).Quote(this);
				}
				yield res+" pronouns.";
			}
		};
	}
}
