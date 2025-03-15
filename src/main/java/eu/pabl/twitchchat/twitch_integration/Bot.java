package eu.pabl.twitchchat.twitch_integration;

import eu.pabl.twitchchat.badge.Badge;
import eu.pabl.twitchchat.badge.BadgeFont;
import eu.pabl.twitchchat.config.ModConfig;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.common.events.domain.EventUser;
import eu.pabl.twitchchat.TwitchChatMod;

public class Bot {
  private TwitchClient twitchClient;
  private final TwitchClientBuilder twitchClientBuilder;
  private final String username;
  private final String oauthKey;
  private String channel;
  private ExecutorService myExecutor;
  private HashMap<String, TextColor> formattingColorCache; // Map of usernames to colors to keep consistency with usernames and colors
  private boolean isConnected;

  public Bot(String username, String oauthKey, String channel) {
    this.channel = channel.toLowerCase();
    this.username = username.toLowerCase();
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
      eventManager.onEvent(ChannelStateEvent.class, this::onJoin);

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
    String message = event.getMessage();
    System.out.println("TWITCH MESSAGE: " + message);
    EventUser user = event.getUser();
    if (user != null) {
      Map<String, String> v3Tags = event.getMessageEvent().getTags();
      if (v3Tags != null) {
        String nick = user.getName();
        if (!ModConfig.getConfig().getIgnoreList().contains(nick)) {
          String colorTag = v3Tags.get("color");
          TextColor formattingColor;
          
          if (isFormattingColorCached(nick)) {
            formattingColor = getFormattingColor(nick);
          } else {
            if (colorTag.equals("")) {
              formattingColor = CalculateMinecraftColor.getDefaultUserColor(nick);
            } else {
              Color userColor = Color.decode(colorTag);
              formattingColor = TextColor.fromRgb(userColor.getRGB());
            }
            putFormattingColor(nick, formattingColor);
          }

          String[] badges = Arrays.stream(Objects.requireNonNull(v3Tags.getOrDefault("badges", "")).split(","))
                  .map(badge -> badge.split("/")[0])
                  .toArray(String[]::new);

          String formattedTime = TwitchChatMod.formatTMISentTimestamp(v3Tags.get("tmi-sent-ts"));
          TwitchChatMod.addTwitchMessage(formattedTime, nick, message, formattingColor, badges, false);
        }
      } else {
        System.out.println("Message with no v3tags: " + event.getMessage());
      }
    } else {
      System.out.println("NON-USER MESSAGE" + event.getMessage());
    }
  }

  public void onConnect(GlobalUserStateEvent event) {
        // Info about our user. More at https://dev.twitch.tv/docs/irc/commands/#userstate
        // Set our correct colour :).
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
    EventUser user = event.getUser();

    if (user != null) {
      String nick = user.getName();

      if (!ModConfig.getConfig().getIgnoreList().contains(nick.toLowerCase())) {
        String formattedTime = TwitchChatMod.formatDateTwitch(event.getFiredAt().getTime());

        TextColor formattingColor;
        if (isFormattingColorCached(nick)) {
          formattingColor = getFormattingColor(nick);
        } else {
          formattingColor = CalculateMinecraftColor.getDefaultUserColor(nick);
          putFormattingColor(nick, formattingColor);
        }

        TwitchChatMod.addTwitchMessage(formattedTime, nick, event.getMessage(), formattingColor, new String[]{}, true);
      }
    } else {
      System.out.println("NON-USER ACTION" + event.getMessage());
    }
  }

  String currentChannel;
  public void onJoin(ChannelStateEvent event) {
    String channel = event.getChannel().getName();
     if (currentChannel == null || !currentChannel.equals(channel)) {
      TwitchChatMod.addNotification(Text.translatable("text.twitchchat.bot.connected", this.channel));
      currentChannel = channel;
    }
  }

  public void sendMessage(String message) {
    twitchClient.getChat().sendMessage(this.channel, message);
  }

  public String getUsername() {
    return username;
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

  public void putFormattingColor(String nick, TextColor color) {
    if (nick == null || nick.equals("") || color == null) return;
    formattingColorCache.put(nick.toLowerCase(), color);
  }
  public void putFormattingColor(String nick, String color) {
    this.putFormattingColor(nick, TextColor.fromRgb(Integer.valueOf(color.replace("#", ""), 16)));
  }
  public TextColor getFormattingColor(String nick) {
    return formattingColorCache.get(nick.toLowerCase());
  }
  public boolean isFormattingColorCached(String nick) {
    return formattingColorCache.containsKey(nick.toLowerCase());
  }

  public void joinChannel(String channel) {
    String oldChannel = this.channel;
    this.channel = channel.toLowerCase();
    if (this.isConnected()) {
      myExecutor.execute(() -> {
        if (twitchClient.getChat().isChannelJoined(oldChannel)) {
          twitchClient.getChat().leaveChannel(oldChannel); // Leave the channel
        }
        twitchClient.getChat().joinChannel(this.channel); // Join the new channel

        String channelID = getUserID(channel);
        twitchClient.getHelix().getGlobalChatBadges(this.oauthKey).execute().getBadgeSets().forEach(chatBadgeSet -> TwitchChatMod.BADGES.add(new Badge(chatBadgeSet)));
        twitchClient.getHelix().getChannelChatBadges(this.oauthKey, channelID).execute().getBadgeSets().forEach(chatBadgeSet -> TwitchChatMod.BADGES.add(channelID, new Badge(chatBadgeSet)));
        BadgeFont.reload();
      });
    }
  }
}
