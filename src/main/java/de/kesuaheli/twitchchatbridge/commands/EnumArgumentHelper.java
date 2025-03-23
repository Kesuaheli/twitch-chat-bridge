package de.kesuaheli.twitchchatbridge.commands;

import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.StringIdentifiable;

public class EnumArgumentHelper<E extends Enum<E> & StringIdentifiable> extends EnumArgumentType<E> {

  EnumArgumentHelper(E[] values) {
    super(
      StringIdentifiable.createCodec(() -> values),
      () -> values);
  }
}
