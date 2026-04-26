package de.kesuaheli.twitchchatbridge.twitch_integration;

import com.github.twitch4j.chat.events.AbstractChannelMessageEvent;
import com.github.twitch4j.helix.domain.User;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import de.kesuaheli.twitchchatbridge.badge.Badge;
import de.kesuaheli.twitchchatbridge.badge.BadgeFont;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.kesuaheli.twitchchatbridge.TwitchChatMod.CONFIG;
import static de.kesuaheli.twitchchatbridge.TwitchChatMod.LOGGER;

public class FormatMessage {

  public static void formatAndSend(AbstractChannelMessageEvent event, boolean isActionMessage) {
    Component formattedMessage = FormatMessage.formatMessage(event, isActionMessage);
    if (formattedMessage == null) return;

    TwitchChatMod.addTwitchMessage(formattedMessage);
  }

  public static void formatAndSend(String message, boolean isActionMessage) {
    formatAndSend(new Date(), TwitchChatMod.bot.getUserBadges(), TwitchChatMod.bot.getUsername(), message, isActionMessage);
  }

  public static void formatAndSend(Date time, List<Badge> badges, String username, String message, boolean isActionMessage) {
    Component formattedMessage = formatMessage(time, getUserAvatarBadge(CONFIG.avatarBadge() ? TwitchChatMod.bot.getChannelID() : null), badges, username, message, isActionMessage);

    TwitchChatMod.addTwitchMessage(formattedMessage);
  }

  public static @Nullable Component formatMessage(AbstractChannelMessageEvent event, boolean isActionMessage) {
    String nick = event.getMessageEvent().getUserDisplayName().orElse(event.getUser().getName());
    if (CONFIG.ignoreList().stream().anyMatch(nick::equalsIgnoreCase)) {
      return null;
    }

    List<Badge> badges = new ArrayList<>();
    event.getMessageEvent().getBadges().forEach((name,  version) -> {
      try {
        Badge badge = TwitchChatMod.BADGES.get(event.getChannel().getId(), name, version);
        badges.add(badge);
      } catch (IllegalArgumentException ignored) {}
    });

    event.getMessageEvent().getUserChatColor().ifPresent(
        colorTag -> TwitchChatMod.bot.putFormattingColor(nick, colorTag)
    );

    return formatMessage(
        event.getFiredAt().getTime(),
        getUserAvatarBadge(event.getSourceChannelId().orElse(CONFIG.avatarBadge() ? event.getMessageEvent().getChannelId() : null)),
        badges,
        nick,
        event.getMessage(),
        isActionMessage
    );
  }

  public static @NotNull Component formatMessage(Date time, Component avatar, List<Badge> badges, String username, String message, boolean isActionMessage) {
    if (!TwitchChatMod.bot.isFormattingColorCached(username)) {
      TwitchChatMod.bot.putFormattingColor(username);
    }

    MutableComponent text = Component.literal(formatDateTwitch(time));

    MutableComponent prefixText = Component.literal(CONFIG.broadcastPrefix()).withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE));
    text.append(prefixText);

    text.append(avatar);

    MutableComponent usernameText = Component.literal("");
    badges.forEach(badge -> usernameText.append(badge.toText()));
    usernameText.append(Component.literal(username).withStyle(style -> style.withColor(TwitchChatMod.bot.getFormattingColor(username))));

    message = sanitiseMessage(message);
    if (isActionMessage) {
      Component messageText = Component.literal(message).withStyle(style -> style.withColor(TwitchChatMod.bot.getFormattingColor(username)));
      text.append(Component.translatable("chat.type.emote", usernameText, messageText));
    }
    else {
      text.append(Component.translatable("options.generic_value", usernameText, message));
    }

    return text;
  }

  private static String sanitiseMessage(String message) {
    return message
      .replaceAll("§", "")
      .replaceAll("\uFFA0", "")
      .trim();
  }

  public static String formatDateTwitch(Date date) {
    SimpleDateFormat sf = new SimpleDateFormat(CONFIG.dateFormat());
    return sf.format(date);
  }

  private static @NotNull Component getUserAvatarBadge(@Nullable String userID) {
    if (userID == null || userID.isEmpty()) return Component.empty();

    User user = TwitchChatMod.bot.getUserByID(userID);
    if (user == null) {
      return Component.empty();
    }

    Badge badge;
    try {
      badge = TwitchChatMod.BADGES.get("@" + user.getLogin(), "");
    } catch (IllegalArgumentException e) {
      try {
        badge = new Badge(user);
      } catch (URISyntaxException | IOException ex) {
        TwitchChatMod.LOGGER.error("Failed to resolve user avatar badge for @{}", user.getLogin());
        return Component.empty();
      }
      TwitchChatMod.BADGES.add(badge);
      BadgeFont.reload();
      TwitchChatMod.LOGGER.info("Added Avatar badge for user {} ({})", user.getDisplayName(), user.getLogin());
    }
    return badge.toText();
  }
}
