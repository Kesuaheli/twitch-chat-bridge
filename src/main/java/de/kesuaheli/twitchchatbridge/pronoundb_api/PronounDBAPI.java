package de.kesuaheli.twitchchatbridge.pronoundb_api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class PronounDBAPI {
	private static final String BASE_URL = "https://pronoundb/api/v2";
	private static final String LOOKUP_FORMAT = BASE_URL+"/lookup?platform=%s&ids=%s";
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	private static final String USER_AGENT = "Twitch-Chat-Bridge/"+TwitchChatMod.VERSION+" Minecraft/"+ Minecraft.getInstance().getLaunchedVersion();

	public static @Nullable PronounSet lookup(@NotNull Platform platform, @NotNull String id) {
		return lookup(platform, new String[]{id}).get(id);
	}

	public static @NotNull Map<String, @Nullable PronounSet> lookup(@NotNull Platform platform, @NotNull String ...ids) {
		URI uri = URI.create(String.format(LOOKUP_FORMAT,
			platform,
			String.join(",", ids)
		));
		HttpRequest request = HttpRequest.newBuilder(uri)
			.GET()
			.setHeader("User-Agent", USER_AGENT)
			.build();

		HttpResponse<InputStream> response;
		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			TwitchChatMod.LOGGER.error("Failed to make lookup request to PronounDB: ", e);
			return Map.of();
		}
		if (response.statusCode() != 200) {
			TwitchChatMod.LOGGER.error("PronounDB lookup request: invalid response code '{}'",
				HttpResponseStatus.valueOf(response.statusCode())
			);
			return Map.of();
		}

		JsonObject json;
		try {
			json = JsonParser.parseReader(new InputStreamReader(response.body())).getAsJsonObject();
		} catch (JsonParseException e) {
			TwitchChatMod.LOGGER.error("PronounDB lookup request: invalid json response: ", e);
			return Map.of();
		}

		HashMap<String, PronounSet> out = new HashMap<>();
		for (String id : ids) {
			if (!json.has(id)) {
				continue;
			}
			out.put(id, new PronounSet(json.getAsJsonObject(id)));
		}
		return out;
	}
}
