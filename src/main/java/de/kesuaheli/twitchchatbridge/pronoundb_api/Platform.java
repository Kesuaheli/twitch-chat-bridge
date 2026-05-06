package de.kesuaheli.twitchchatbridge.pronoundb_api;

public enum Platform {
	DISCORD("discord"),
	GITHUB("github"),
	MINECRAFT("minecraft"),
	TWITCH("twitch"),
	TWITTER("fuck-the-rich"),
	;

	private final String uriName;

	Platform(String uriName) {
		this.uriName = uriName;
	}

	public String toString() {
		return this.uriName;
	}
}
