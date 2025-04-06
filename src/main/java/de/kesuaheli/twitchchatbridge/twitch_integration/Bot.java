package de.kesuaheli.twitchchatbridge.twitch_integration;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.helix.domain.User;
import de.kesuaheli.twitchchatbridge.TwitchChatMod;
import de.kesuaheli.twitchchatbridge.badge.Badge;
import de.kesuaheli.twitchchatbridge.badge.BadgeFont;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Bot {
  private TwitchClient twitchClient;
  private final TwitchClientBuilder twitchClientBuilder;
  private String username;
  private List<Badge> userBadges;
  private Map<String, String> pendingBadges;
  private final String oauthKey;
  private String channel;
  private String channelID;
  private final ExecutorService myExecutor;
  private final HashMap<String, TextColor> formattingColorCache; // Map of usernames to colors to keep consistency with usernames and colors
  private boolean isConnected;

  public Bot(String oauthKey, String channel) {
    this.channel = channel.toLowerCase();
    this.oauthKey = oauthKey.replaceFirst("^oauth:", "");
    formattingColorCache = new HashMap<>();

    OAuth2Credential credential = new OAuth2Credential("twitch", oauthKey);
    twitchClientBuilder = TwitchClientBuilder.builder()
        .withChatAccount(credential)
        .withEnableChat(true)
        .withEnableHelix(true)
        ;

    this.myExecutor = Executors.newCachedThreadPool();
  }

  public void start() {
    System.out.println("TWITCH BOT STARTED");
    myExecutor.execute(() -> {
      isConnected = true;
      twitchClient = twitchClientBuilder.build();
      EventManager eventManager = twitchClient.getEventManager();
      eventManager.onEvent(ChannelMessageEvent.class, this::onMessage);
      eventManager.onEvent(GlobalUserStateEvent.class, this::onConnect);
      eventManager.onEvent(ChannelNoticeEvent.class, this::onNotice);
      eventManager.onEvent(ChannelLeaveEvent.class, this::onDisconnect);
      eventManager.onEvent(ChannelMessageActionEvent.class, this::onAction);
      eventManager.onEvent(UserStateEvent.class, this::onUserstate);
      eventManager.onEvent(ChannelJoinEvent.class, this::onChannelJoin);

      if (!Objects.equals(this.channel, "")) {
        joinChannel(channel);
      }
    });
  }

  public void stop() {
    isConnected = false;
    twitchClient.close();
  }

  public boolean isConnected() {
    return isConnected;
  }

  public void onMessage(ChannelMessageEvent event) {
    FormatMessage.formatAndSend(event, false);
  }

  public void onConnect(GlobalUserStateEvent event) {
    this.username = event.getDisplayName().orElse(event.getMessageEvent().getUserName());
    this.userBadges = new ArrayList<>();
    event.getMessageEvent().getBadges().forEach((name,  version) -> {
      try {
        Badge badge = TwitchChatMod.BADGES.get(name);
        this.userBadges.add(badge);
      } catch (IllegalArgumentException ignored) {}
    });

    String colorTag = event.getColor().orElse(null);
    if (colorTag != null) {
      Color userColor = Color.decode(colorTag);
      TextColor formattingColor = TextColor.fromRgb(userColor.getRGB());

      putFormattingColor(getUsername(), formattingColor);
    }
  }

  public void onNotice(ChannelNoticeEvent event) {
    System.out.println("TWITCH NOTICE: " + event.toString());
    TwitchChatMod.addNotification(Text.literal(event.getMessage()));
  }


  public void onDisconnect(ChannelLeaveEvent event) {
    System.out.println("TWITCH DISCONNECT: " + event.toString());
  }

  // Handle /me
  public void onAction(ChannelMessageActionEvent event) {
    FormatMessage.formatAndSend(event, true);
  }

  public void onUserstate(UserStateEvent event) {
    this.pendingBadges = event.getMessageEvent().getBadges();
  }

  public void onChannelJoin(ChannelJoinEvent event) {
    if (event.getUser().getName().equalsIgnoreCase(this.username)) {
      this.onChannelJoinMe(event);
    }
  }

  private void onChannelJoinMe(ChannelJoinEvent event) {
    String ID = getUserID(event.getChannel().getName());
    if (Objects.equals(this.channelID, ID)) return;
    this.channelID = ID;
    TwitchChatMod.addNotification(Text.translatable("text.twitchchat.bot.connected", event.getChannel().getName()));
  }

  public void sendMessage(String message) {
    twitchClient.getChat().sendMessage(this.channel, message);
  }

  public String getUsername() {
    return username;
  }
  public List<Badge> getUserBadges() {
    return this.userBadges;
  }

  public String getChannelID() {
    return this.channelID;
  }

  public String getUserID() {
    return getUserID(null);
  }
  public String getUserID(String username) {
    if (username == null) username = this.username;

    return twitchClient.getHelix()
        .getUsers(this.oauthKey, null, List.of(username)).execute()
        .getUsers().getFirst()
        .getId();
  }

  public @Nullable User getUserByID(@NotNull String userID) {
    try {
      return twitchClient.getHelix()
        .getUsers(this.oauthKey, List.of(userID), null).execute()
        .getUsers().getFirst();
    } catch (NoSuchElementException e) {
      TwitchChatMod.LOGGER.warn("Failed to get Twitch user for ID " + userID + ": " + e.getMessage());
      return null;
    }
  }

  public void putFormattingColor(String nick, TextColor color) {
    if (nick == null || nick.equals("") || color == null) return;
    formattingColorCache.put(nick.toLowerCase(), color);
  }
  public void putFormattingColor(String nick, String color) {
    this.putFormattingColor(nick, TextColor.fromRgb(Color.decode(color).getRGB()));
  }
  public void putFormattingColor(String nick) {
    this.putFormattingColor(nick, CalculateMinecraftColor.getDefaultUserColor(nick));
  }

  public TextColor getFormattingColor(String nick) {
    return formattingColorCache.get(nick.toLowerCase());
  }
  public boolean isFormattingColorCached(String nick) {
    return formattingColorCache.containsKey(nick.toLowerCase());
  }

  public void joinChannel(String channel) {
    if (!this.isConnected()) return;
    String oldChannel = this.channel;
    this.channel = channel.toLowerCase();

    myExecutor.execute(() -> {
      if (twitchClient.getChat().isChannelJoined(oldChannel)) {
        twitchClient.getChat().leaveChannel(oldChannel); // Leave the channel
      }
      twitchClient.getChat().joinChannel(this.channel); // Join the new channel

      String channelID = getUserID(channel);
      twitchClient.getHelix().getGlobalChatBadges(this.oauthKey).execute().getBadgeSets().forEach(chatBadgeSet -> TwitchChatMod.BADGES.add(new Badge(chatBadgeSet)));
      twitchClient.getHelix().getChannelChatBadges(this.oauthKey, channelID).execute().getBadgeSets().forEach(chatBadgeSet -> TwitchChatMod.BADGES.add(channelID, new Badge(chatBadgeSet)));
      BadgeFont.reload();

      handlePendingBadges();
    });
  }

  private void handlePendingBadges() {
    if (this.pendingBadges == null ) return;

    this.userBadges = new ArrayList<>();
    this.pendingBadges.forEach((name, version) -> {
      try {
        Badge badge = TwitchChatMod.BADGES.get(this.channelID, name);
        this.userBadges.add(badge);
      } catch (IllegalArgumentException ignored) {}
    });
    this.pendingBadges = null;
  }
}
