package de.kesuaheli.twitchchatbridge.pronoundb_api;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PronounSet {
	private final Map<Locale, List<Pronoun>> pronouns;

	public PronounSet(@NotNull JsonObject json) {
		this.pronouns = HashMap.newHashMap(json.size());

		for (String jsonLocale : json.keySet()) {
			Locale locale = Locale.fromJSONName(jsonLocale);
			List<Pronoun> pronounList = json.getAsJsonArray(jsonLocale)
					.asList()
					.stream()
					.map(jsonPronoun ->  Pronoun.fromString(jsonPronoun.getAsString(), locale))
					.toList();
			this.pronouns.put(locale, pronounList);
		}
	}

	public @Nullable String Normal(@NotNull Locale locale) {
		List<Pronoun> pronounList = this.pronouns.get(locale);
		if (pronounList == null || pronounList.isEmpty()) return null;

		if (pronounList.size() == 1) return pronounList.getFirst().Normal(locale);

		return pronounList.stream()
			.map(p -> p.Short(locale))
			.collect(Collectors.joining("/"));
	}

	public @Nullable String Short(@NotNull Locale locale) {
		List<Pronoun> pronounList = this.pronouns.get(locale);
		if (pronounList == null || pronounList.isEmpty()) return null;

		if (pronounList.size() == 1) return pronounList.getFirst().Normal(locale);

		return pronounList.stream()
			.limit(2)
			.map(p -> p.Short(locale))
			.collect(Collectors.joining("/"));
	}

	public @Nullable String Long(@NotNull Locale locale) {
		List<Pronoun> pronounList = this.pronouns.get(locale);
		if (pronounList == null || pronounList.isEmpty()) return null;
		return locale.formatLong(pronounList);
	}
}
