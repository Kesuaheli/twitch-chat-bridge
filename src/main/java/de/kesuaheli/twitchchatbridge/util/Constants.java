package de.kesuaheli.twitchchatbridge.util;

import net.minecraft.resources.Identifier;

public class Constants {
    public static final String NAMESPACE = "twitchchat";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }
}
