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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
    Text formattedMessage = FormatMessage.formatMessage(event, isActionMessage);
    if (formattedMessage == null) return;

    TwitchChatMod.addTwitchMessage(formattedMessage);
  }

  public static void formatAndSend(String message, boolean isActionMessage) {
    formatAndSend(new Date(), TwitchChatMod.bot.getUserBadges(), TwitchChatMod.bot.getUsername(), TwitchChatMod.bot.getUserID(), message, isActionMessage);
  }

  public static void formatAndSend(Date time, List<Badge> badges, String username, String userID, String message, boolean isActionMessage) {
    Text formattedMessage = formatMessage(time, getUserAvatarBadge(CONFIG.avatarBadge() ? TwitchChatMod.bot.getChannelID() : null), badges, username, userID, message, isActionMessage);

    TwitchChatMod.addTwitchMessage(formattedMessage);
  }

  public static @Nullable Text formatMessage(AbstractChannelMessageEvent event, boolean isActionMessage) {
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

  public static @NotNull Text formatMessage(Date time, Text avatar, List<Badge> badges, String username, String userID, String message, boolean isActionMessage) {
    if (!TwitchChatMod.bot.isFormattingColorCached(username)) {
      TwitchChatMod.bot.putFormattingColor(username);
    }

    MutableText text = Text.literal(formatDateTwitch(time));

    MutableText prefixText = Text.literal(CONFIG.broadcastPrefix()).formatted(Formatting.DARK_PURPLE);
    text.append(prefixText);

    text.append(avatar);

    MutableText usernameText = Text.literal("");
    MutableText pronounText = appendPronouns(userID);
    if (pronounText != null && CONFIG.showPronounsInline()) {
      usernameText.append(pronounText);
    }
    badges.forEach(badge -> usernameText.append(badge.toText()));
    usernameText.append(Text.literal(username).styled(style -> {
      if (pronounText != null) {
        style = style.withHoverEvent(pronounText.getStyle().getHoverEvent());
      } else {
        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("This user didn't specify pronouns on PronounDB.org yet.")));
      }
      return style.withColor(TwitchChatMod.bot.getFormattingColor(username));
	}));

    message = sanitiseMessage(message);
    if (isActionMessage) {
      Text messageText = Text.literal(message).styled(style -> style.withColor(TwitchChatMod.bot.getFormattingColor(username)));
      text.append(Text.translatable("chat.type.emote", usernameText, messageText));
    }
    else {
      text.append(Text.translatable("options.generic_value", usernameText, message));
    }

    return text;
  }

  private static MutableText appendPronouns(String userID) {
    PronounSet pronouns = PronounDBAPI.lookup(Platform.TWITCH, userID);
    if (pronouns == null) return null;

    String pronounShort = pronouns.Short(Locale.EN);
    if (pronounShort == null) return null;
    String pronounNormal = pronouns.Normal(Locale.EN);
    if (pronounNormal == null) pronounNormal = pronounShort;
    String pronounLong = pronouns.Long(Locale.EN);

    MutableText pronounText = Text.literal("["+pronounShort+"]");
    pronounText = pronouns.decoration.decor(pronounText);

    MutableText description = Text.literal("");
    description.append(Text.literal(pronounNormal).formatted(Formatting.DARK_GRAY));
    if (pronounLong != null) {
      description.append("\n").append(pronounLong);
    }
    pronounText.styled(style -> style
      .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, description))
      .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://pronoundb.org"))
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

  private static @NotNull Text getUserAvatarBadge(@Nullable String userID) {
    if (userID == null || userID.isEmpty()) return Text.empty();

    User user = TwitchChatMod.bot.getUserByID(userID);
    if (user == null) {
      return Text.empty();
    }

    Badge badge;
    try {
      badge = TwitchChatMod.BADGES.get("@" + user.getLogin(), "");
    } catch (IllegalArgumentException e) {
      try {
        badge = new Badge(user);
      } catch (URISyntaxException | IOException ex) {
        LOGGER.error("Failed to resolve user avatar badge for @{}", user.getLogin());
        return Text.empty();
      }
      TwitchChatMod.BADGES.add(badge);
      BadgeFont.reload();
      LOGGER.info("Added Avatar badge for user {} ({})", user.getDisplayName(), user.getLogin());
    }
    return badge.toText();
  }
}
