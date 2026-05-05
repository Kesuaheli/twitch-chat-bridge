package de.kesuaheli.twitchchatbridge.pronoundb_api;

import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import net.minecraft.client.Minecraft;

import java.net.http.HttpClient;

public class PronounDBAPI {
	private static final String BASE_URL = "https://pronoundb/api/v2";
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	private static final String USER_AGENT = "Twitch-Chat-Bridge/"+TwitchChatMod.VERSION+" Minecraft/"+ Minecraft.getInstance().getLaunchedVersion();
}
