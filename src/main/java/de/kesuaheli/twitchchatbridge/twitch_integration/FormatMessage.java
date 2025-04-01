package de.kesuaheli.twitchchatbridge.twitch_integration;

import com.github.twitch4j.chat.events.AbstractChannelMessageEvent;
import com.github.twitch4j.helix.domain.User;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import de.kesuaheli.twitchchatbridge.badge.Badge;
import de.kesuaheli.twitchchatbridge.badge.BadgeFont;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;

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
    Text formattedMessage = formatMessage(time, Text.empty(), badges, username, message, isActionMessage);

    TwitchChatMod.addTwitchMessage(formattedMessage);
  }

  public static @Nullable Text formatMessage(AbstractChannelMessageEvent event, boolean isActionMessage) {
    String nick = event.getMessageEvent().getUserDisplayName().orElse(event.getUser().getName());
    if (CONFIG.ignoreList().stream().anyMatch(nick::equalsIgnoreCase)) {
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
        getUserAvatarBadge(event.getSourceChannelId().orElse(null)),
        badges,
        nick,
        event.getMessage(),
        isActionMessage
    );
  }

  public static @NotNull Text formatMessage(Date time, Text avatar, List<Badge> badges, String username, String message, boolean isActionMessage) {
    if (!TwitchChatMod.bot.isFormattingColorCached(username)) {
      TwitchChatMod.bot.putFormattingColor(username);
    }

    MutableText text = Text.literal(formatDateTwitch(time));

    MutableText prefixText = Text.literal(CONFIG.prefix()).styled(style -> style.withColor(Formatting.DARK_PURPLE));
    text.append(prefixText);

    text.append(avatar);

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
    SimpleDateFormat sf = new SimpleDateFormat(CONFIG.dateFormat());
    return sf.format(date);
  }

  private static @NotNull Text getUserAvatarBadge(@Nullable String userID) {
    if (userID == null) return Text.empty();

    try {
      Badge badge = TwitchChatMod.BADGES.getChannelOnly(userID, "");
      return badge.toText();
    } catch (IllegalArgumentException ignored) {}


    User user = TwitchChatMod.bot.getUserByID(userID);
    if (user == null) {
      return Text.empty();
    }

    TwitchChatMod.BADGES.add(userID, new Badge(user));
    BadgeFont.reload();
    TwitchChatMod.LOGGER.info("Added Avatar badge for user " + user.getDisplayName());
    try {
      return TwitchChatMod.BADGES.getChannelOnly(userID, "").toText();
    } catch (IllegalArgumentException e) {
      TwitchChatMod.LOGGER.error("Newly added Avatar badge for user " + user.getDisplayName() + " not found:" + e);
      return Text.empty();
    }
  }
}
