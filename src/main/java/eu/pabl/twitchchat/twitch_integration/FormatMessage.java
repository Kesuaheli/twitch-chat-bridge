package eu.pabl.twitchchat.twitch_integration;

import com.github.twitch4j.chat.events.AbstractChannelMessageEvent;
import eu.pabl.twitchchat.TwitchChatMod;
import eu.pabl.twitchchat.badge.Badge;
import eu.pabl.twitchchat.config.ModConfig;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FormatMessage {

  public static void formatAndSend(AbstractChannelMessageEvent event, boolean isActionMessage) {
    Text formattedMessage = FormatMessage.formatMessage(event, isActionMessage);
    if (formattedMessage == null) return;

    TwitchChatMod.addTwitchMessage(formattedMessage);
  }

  public static void formatAndSend(String message, boolean isActionMessage) {
    formatAndSend(new Date(), TwitchChatMod.bot.getUserBadges(), TwitchChatMod.bot.getUsername(), message, isActionMessage);
  }

  public static void formatAndSend(Date time, List<Badge> badges, String username, String message, boolean isActionMessage) {
    Text formattedMessage = formatMessage(time, badges, username, message, isActionMessage);

    TwitchChatMod.addTwitchMessage(formattedMessage);
  }

  public static @Nullable Text formatMessage(AbstractChannelMessageEvent event, boolean isActionMessage) {
    String nick = event.getMessageEvent().getUserDisplayName().orElse(event.getUser().getName());
    if (ModConfig.getConfig().getIgnoreList().stream().anyMatch(nick::equalsIgnoreCase)) {
      return null;
    }

    List<Badge> badges = new ArrayList<>();
    event.getMessageEvent().getBadges().forEach((name,  version) -> {
      try {
        Badge badge = TwitchChatMod.BADGES.get(event.getChannel().getId(), name);
        badges.add(badge);
      } catch (IllegalArgumentException ignored) {}
    });

    event.getMessageEvent().getUserChatColor().ifPresent(
        colorTag -> TwitchChatMod.bot.putFormattingColor(nick, colorTag)
    );

    return formatMessage(
        event.getFiredAt().getTime(),
        badges,
        nick,
        event.getMessage(),
        isActionMessage
    );
  }

  public static @NotNull Text formatMessage(Date time, List<Badge> badges, String username, String message, boolean isActionMessage) {
    if (!TwitchChatMod.bot.isFormattingColorCached(username)) {
      TwitchChatMod.bot.putFormattingColor(username);
    }

    MutableText text = Text.literal(formatDateTwitch(time));

    MutableText prefixText = Text.literal(ModConfig.getConfig().getBroadcastPrefix()).styled(style -> style.withColor(Formatting.DARK_PURPLE));
    text.append(prefixText);

    MutableText usernameText = Text.literal("");
    badges.forEach(badge -> usernameText.append(badge.toText()));
    usernameText.append(Text.literal(username).styled(style -> style.withColor(TwitchChatMod.bot.getFormattingColor(username))));

    if (isActionMessage) {
      Text messageText = Text.literal(message).styled(style -> style.withColor(TwitchChatMod.bot.getFormattingColor(username)));
      text.append(Text.translatable("chat.type.emote", usernameText, messageText));
    }
    else {
      text.append(Text.translatable("options.generic_value", usernameText, message));
    }

    return text;
  }

  private static String sanitiseMessage(String message) {
    return message.replaceAll("§", "");
  }

  public static String formatDateTwitch(Date date) {
    SimpleDateFormat sf = new SimpleDateFormat(ModConfig.getConfig().getDateFormat());
    return sf.format(date);
  }
}
