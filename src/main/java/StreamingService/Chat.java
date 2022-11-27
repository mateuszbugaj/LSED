package StreamingService;

import Utils.Subscriber;
import Utils.Publisher;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class Chat implements Publisher {
    private static final Logger logger = LoggerFactory.getLogger(Chat.class);
    private final String chatName;
    private ChatService service;
    private final ArrayList<UserMessage> receivedMessages = new ArrayList<>(); // Chat equivalent of the log message received from the device
    private final ArrayList<Subscriber<UserMessage>> receivedMessageSubscriber = new ArrayList<>();


    public Chat(String configFile) throws FileNotFoundException {
        logger.info("Configuring chat from file: " + configFile);

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(new FileReader(configFile));

        String serviceName = (String) data.get("service"); // todo: this should be enum or something like that (list of available services and their names?)
        chatName = (String) data.get("name");

        if(serviceName.toLowerCase(Locale.ROOT).equals("twitch")){
            String token = (String) data.get("token");
            service = new Twitch(chatName, token);
//            service.addMessageListener(ircMessageEvent -> { // todo: put this in the factory pattern
//                receivedMessageSubscriber.forEach(s -> s.update(new UserMessage(ircMessageEvent.getUserName(), ircMessageEvent.getMessage().get(), ircMessageEvent.getFiredAt().getTime())));
//            });
            service.addNewMessageSubscription(receivedMessageSubscriber);
        } else if(serviceName.toLowerCase(Locale.ROOT).equals("youtube")){
            String token = (String) data.get("token");
            String channelId = (String) data.get("channelId");
            service = new Youtube(channelId, token);
//            service.addMessageListener(ircMessageEvent -> {
//                System.out.println("YouTube message event");
//                receivedMessageSubscriber.forEach(s -> s.update(new UserMessage(ircMessageEvent.getUserName(), ircMessageEvent.getMessage().get(), ircMessageEvent.getFiredAt().getTime())));
//            });
            service.addNewMessageSubscription(receivedMessageSubscriber);
        }
    }

    public String getChatName() {
        return chatName;
    }

    public void sendMessage(String message){
        System.out.println("Sending: " + message);
        service.sendMessage(message);
    }

    public Image getIcon(){
        return service.getIcon();
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {
        receivedMessageSubscriber.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        receivedMessageSubscriber.remove(subscriber);
    }
}
