package de.kesuaheli.twitchchatbridge.util;

import net.minecraft.util.Identifier;

public class Constants {
    public static final String NAMESPACE = "twitchchat";

    public static net.minecraft.util.Identifier id(String path) {
        return Identifier.of(NAMESPACE, path);
    }
}
