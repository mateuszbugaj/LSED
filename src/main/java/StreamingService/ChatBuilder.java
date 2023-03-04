package StreamingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Locale;
import java.util.Map;

public class ChatBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ChatBuilder.class);
    private final ChatManagerMediator mediator;

    public ChatBuilder(ChatManagerMediator mediator) {
        this.mediator = mediator;
    }

    public ChatService build(String configFile) throws FileNotFoundException {
        logger.info("Configuring chat from file: " + configFile);
        ChatService service = null;

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(new FileReader(configFile));

        String serviceName = (String) data.get("service"); // todo: this should be enum or something like that (list of available services and their names?)
        String chatName = (String) data.get("name");

        if(serviceName.toLowerCase(Locale.ROOT).equals("twitch")){
            String token = (String) data.get("token");
            service = new Twitch(chatName, chatName, token, mediator);
        } else if(serviceName.toLowerCase(Locale.ROOT).equals("youtube")){
            String token = (String) data.get("token");
            String channelId = (String) data.get("channelId");
            service = new Youtube(chatName, channelId, token, mediator);
        }

        return service;
    }
}
