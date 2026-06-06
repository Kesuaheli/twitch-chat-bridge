package de.kesuaheli.twitchchatbridge.twitch_integration;

import com.github.twitch4j.chat.events.AbstractChannelMessageEvent;
import com.github.twitch4j.helix.domain.User;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import de.kesuaheli.twitchchatbridge.badge.Badge;
import de.kesuaheli.twitchchatbridge.badge.BadgeFont;
import de.kesuaheli.twitchchatbridge.pronoundb_api.Locale;
import de.kesuaheli.twitchchatbridge.pronoundb_api.Platform;
import de.kesuaheli.twitchchatbridge.pronoundb_api.PronounDBAPI;
import de.kesuaheli.twitchchatbridge.pronoundb_api.PronounSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
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
    formatAndSend(new Date(), TwitchChatMod.bot.getUserBadges(), TwitchChatMod.bot.getUsername(), TwitchChatMod.bot.getUserID(), message, isActionMessage);
  }

  public static void formatAndSend(Date time, List<Badge> badges, String username, String userID, String message, boolean isActionMessage) {
    Component formattedMessage = formatMessage(time, getUserAvatarBadge(CONFIG.avatarBadge() ? TwitchChatMod.bot.getChannelID() : null), badges, username, userID, message, isActionMessage);

    TwitchChatMod.addTwitchMessage(formattedMessage);
  }

  public static @Nullable Component formatMessage(AbstractChannelMessageEvent event, boolean isActionMessage) {
    String nick = event.getMessageEvent().getUserDisplayName().orElse(event.getUser().getName());
    if (CONFIG.ignoreList().stream().anyMatch(nick::equalsIgnoreCase)) {
      return null;
    }

    List<Badge> badges = new ArrayList<>();
    final boolean[] isFounder = {false};
    event.getMessageEvent().getBadges().forEach((name,  version) -> {
      if (isFounder[0] && name.equals("subscriber")) {
        return;
      } else if (name.equals("founder")) {
        isFounder[0] = true;
        badges.removeIf(b -> b.getName().equals("subscriber"));
      }
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
        event.getMessageEvent().getUserId(),
        event.getMessage(),
        isActionMessage
    );
  }

  public static @NotNull Component formatMessage(Date time, Component avatar, List<Badge> badges, String username, String userID, String message, boolean isActionMessage) {
    if (!TwitchChatMod.bot.isFormattingColorCached(username)) {
      TwitchChatMod.bot.putFormattingColor(username);
    }

    MutableComponent text = Component.literal(formatDateTwitch(time));

    MutableComponent prefixText = Component.literal(CONFIG.broadcastPrefix()).withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE));
    text.append(prefixText);

    text.append(avatar);

    MutableComponent usernameText = Component.literal("");
    MutableComponent pronounText = appendPronouns(userID);
    if (pronounText != null && CONFIG.showPronounsInline()) {
      usernameText.append(pronounText);
    }
    badges.forEach(badge -> usernameText.append(badge.toText()));
    usernameText.append(Component.literal(username).withStyle(style -> {
      if (pronounText != null) {
        style = style.withHoverEvent(pronounText.getStyle().getHoverEvent());
      } else {
        style = style.withHoverEvent(new HoverEvent.ShowText(Component.literal("This user didn't specify pronouns on PronounDB.org yet.")));
      }
      return style.withColor(TwitchChatMod.bot.getFormattingColor(username));
	}));

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

  private static MutableComponent appendPronouns(String userID) {
    PronounSet pronouns = PronounDBAPI.lookup(Platform.TWITCH, userID);
    if (pronouns == null) return null;

    String pronounShort = pronouns.Short(Locale.EN);
    if (pronounShort == null) return null;
    String pronounNormal = pronouns.Normal(Locale.EN);
    if (pronounNormal == null) pronounNormal = pronounShort;
    String pronounLong = pronouns.Long(Locale.EN);

    MutableComponent pronounText = Component.literal("["+pronounShort+"]");
    pronounText = pronouns.decoration.decor(pronounText);

    MutableComponent description = Component.literal("");
    description.append(Component.literal(pronounNormal).withStyle(ChatFormatting.DARK_GRAY));
    if (pronounLong != null) {
      description.append("\n").append(pronounLong);
    }
    pronounText.withStyle(style -> style
      .withHoverEvent(new HoverEvent.ShowText(description))
      .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://pronoundb.org")))
    );
    return pronounText;
  }

  private static String sanitiseMessage(String message) {
    return message
      .replaceAll("§", "")
      .replaceAll("\uFFA0|\u034F", "")
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
        LOGGER.error("Failed to resolve user avatar badge for @{}", user.getLogin());
        return Component.empty();
      }
      TwitchChatMod.BADGES.add(badge);
      BadgeFont.reload();
      LOGGER.info("Added Avatar badge for user {} ({})", user.getDisplayName(), user.getLogin());
    }
    return badge.toText();
  }
}
