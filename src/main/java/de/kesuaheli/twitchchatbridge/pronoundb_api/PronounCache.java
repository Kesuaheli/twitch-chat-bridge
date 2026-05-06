package de.kesuaheli.twitchchatbridge.pronoundb_api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PronounCache {
	private static final HashMap<Platform, @NotNull HashMap<String, @Nullable PronounSet>> CACHE = HashMap.newHashMap(Platform.values().length);
	static {
		for (Platform platform : Platform.values()) {
			CACHE.put(platform, new HashMap<>());
		}
	}

	public static void store(@NotNull Platform platform, @NotNull String id, @Nullable PronounSet pronouns) {
		CACHE.get(platform).put(id, pronouns);
	}

	public static void store(@NotNull Platform platform, @NotNull String id) {
		store(platform, id, null);
	}

	public static @Nullable PronounSet get(@NotNull Platform platform, @NotNull String id) {
		return CACHE.get(platform).get(id);
	}

	public static @NotNull Map<String, @Nullable PronounSet> get(@NotNull Platform platform, @NotNull String ...ids) {
		HashMap<String, PronounSet> out = HashMap.newHashMap(ids.length);
		for (String id : ids) {
			out.put(id, get(platform, id));
		}
		return out;
	}

	public static boolean isCached(@NotNull Platform platform, @NotNull String id) {
		return CACHE.get(platform).containsKey(id);
	}

	public static @NotNull CacheSplit split(@NotNull Platform platform, @NotNull String ...ids) {
		List<String> uncached = new ArrayList<>();
		HashMap<String, PronounSet> cached = new HashMap<>();
		List<String> emptyCache = new ArrayList<>();

		for (String id : ids) {
			if (!isCached(platform, id)) {
				uncached.add(id);
				continue;
			}
			PronounSet pronouns = get(platform, id);
			if (pronouns == null) {
				emptyCache.add(id);
				continue;
			}
			cached.put(id, pronouns);
		}
		return new CacheSplit(uncached, cached, emptyCache);
	}

	public static class CacheSplit {
		public final List<@NotNull String> uncached;
		public final HashMap<String, @NotNull PronounSet> cached;
		public final List<@NotNull String> emptyCache;

		CacheSplit(List<String> uncached, HashMap<String, @NotNull PronounSet> cached, List<String> emptyCache) {
			this.uncached = uncached;
			this.cached = cached;
			this.emptyCache = emptyCache;
		}
	}
}
