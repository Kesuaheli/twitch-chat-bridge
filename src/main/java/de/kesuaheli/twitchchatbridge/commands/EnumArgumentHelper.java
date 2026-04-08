package de.kesuaheli.twitchchatbridge.commands;

import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.StringRepresentable;

public class EnumArgumentHelper<E extends Enum<E> & StringRepresentable> extends StringRepresentableArgument<E> {

  EnumArgumentHelper(E[] values) {
    super(
      StringRepresentable.fromEnum(() -> values),
      () -> values);
  }
}
