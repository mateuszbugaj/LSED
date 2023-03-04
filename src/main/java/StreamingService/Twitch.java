package StreamingService;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import javafx.scene.image.Image;

import java.util.function.Consumer;

public class Twitch implements ChatService{
    private final String serviceName;
    private final TwitchClient twitchClient;
    private final Image serviceIcon;
    public Twitch(String serviceName, String channelName, String channelToken, ChatManagerMediator mediator) {
        this.serviceName = serviceName;
        serviceIcon = new Image("https://cdn-icons-png.flaticon.com/512/5968/5968819.png", 20, 20, true, true);

        twitchClient = TwitchClientBuilder
                .builder()
                .withEnableHelix(true)
                .withEnableChat(true)
                .withChatAccount(new OAuth2Credential(channelName, channelToken))
                .build();

        twitchClient.getEventManager().onEvent(IRCMessageEvent.class, new Consumer<IRCMessageEvent>() {
            @Override
            public void accept(IRCMessageEvent ircMessageEvent) {
                if (ircMessageEvent.getMessage().isPresent()) {
                    mediator.handleNewMessage(ircMessageEvent.getMessage().get(), ircMessageEvent.getUserName());
                }
            }
        });
    }

    @Override
    public String getName() {
        return serviceName;
    }

    @Override
    public Image getIcon() {
        return serviceIcon;
    }
}


//public class Twitch implements ChatService{
//    private final String channelName;
//    private final TwitchClient twitchClient;
//    private final Image serviceIcon; // todo: move it to the ChatState class or something so the view and GUI is separated
//    public Twitch(String channelName, String channelToken) {
//        this.channelName = channelName;
//        serviceIcon = new Image("https://cdn-icons-png.flaticon.com/512/5968/5968819.png", 20, 20, true, true); // todo: put this in the factory pattern
//
//        twitchClient = TwitchClientBuilder
//                .builder()
//                .withEnableHelix(true)
//                .withEnableChat(true)
//                .withChatAccount(new OAuth2Credential(channelName, channelToken))
//                .build();
//    }
//
//    @Override
//    public void addNewMessageSubscription(ArrayList<Subscriber<UserMessage>> subscribers) {
//        twitchClient.getEventManager().onEvent(IRCMessageEvent.class, new Consumer<IRCMessageEvent>() {
//            @Override
//            public void accept(IRCMessageEvent ircMessageEvent) {
//                if(ircMessageEvent.getMessage().isPresent()){
//                    subscribers.forEach(s -> s.update(new UserMessage(UserManager.getUser(ircMessageEvent.getUserName()), ircMessageEvent.getMessage().get(), ircMessageEvent.getFiredAt().getTime())));
//                }
//            }
//        });
//    }
//
//    @Override
//    public void sendMessage(String message) {
//        twitchClient.getChat().sendMessage(channelName, message);
//    }
//
//    @Override
//    public Image getIcon() {
//        return serviceIcon;
//    }
//}
