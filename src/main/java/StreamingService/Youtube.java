package StreamingService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Youtube implements ChatService{
    private final String serviceName;
    private final String channelId;
    private final Image serviceIcon;
    private String videoId;
    private String liveChatId;
    private Thread thread;
    private final String channelApiKey;
    private final ArrayList<Message> messages = new ArrayList<>();
    private final ChatManagerMediator mediator;

    public Youtube(String serviceName, String channelId, String channelApiKey, ChatManagerMediator mediator) {
        this.serviceName = serviceName;
        this.channelId = channelId;
        this.channelApiKey = channelApiKey;
        this.mediator = mediator;
        serviceIcon = new Image("https://cdn-icons-png.flaticon.com/512/1384/1384060.png", 20, 20, true, true); // todo: put this in the factory pattern

        try {
            // find livestream ID
            JsonNode channelDetails = getResponse(composeUrl("search?part=snippet&channelId=" + channelId + "&order=date&type=video&key=" + channelApiKey));
            for(JsonNode channelContent:channelDetails.get("items")){
                if(!Objects.equals(channelContent.get("snippet").get("liveBroadcastContent").asText(), "none")){
                    videoId = channelContent.get("id").get("videoId").asText();
                }
            }

            // find activeLiveChatId
            JsonNode liveStreamDetails = getResponse(composeUrl("videos?part=liveStreamingDetails,snippet&id=" + videoId + "&key=" + channelApiKey));
            liveChatId = liveStreamDetails.get("items").get(0).get("liveStreamingDetails").get("activeLiveChatId").asText();

            // get messages
            getMessages().forEach(i -> System.out.println(i.toString()));

        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Error while connecting to Youtube API");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private JsonNode getResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(String.valueOf(content));
    }

    private String composeUrl(String content){
        return "https://www.googleapis.com/youtube/v3/" + content;
    }

    private ArrayList<Message> getMessages() throws IOException, ParseException {
        ArrayList<Message> messages = new ArrayList<>();
        JsonNode liveChatMessages = getResponse(composeUrl("liveChat/messages?liveChatId=" + liveChatId + "&part=snippet,authorDetails&maxResults=2000&key=" + channelApiKey));

        for(JsonNode messageInfo: liveChatMessages.get("items")){
            String userName = messageInfo.get("authorDetails").get("displayName").asText();
            String content = messageInfo.get("snippet").get("displayMessage").asText();
            Date messageTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(messageInfo.get("snippet").get("publishedAt").asText());
//            messages.add(new UserMessage(UserManager.getUser(userName), content, messageTime));
            mediator.handleNewMessage(content, userName);
        }

        return messages;
    }

    // todo: fix implementation
//    @Override
//    public void addNewMessageSubscription(ArrayList<Subscriber<UserMessage>> subscribers) {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//                    try {
//                        ArrayList<UserMessage> messages = getMessages();
//
//                        // todo: this could be replaced with Stack
//                        // go through received messages and find new ones
//                        for(UserMessage m:messages){
//                            boolean exists = false;
//                            for (UserMessage m2:userMessages){
//                                if(m.getUser().equals(m2.getUser()) && m.getContent().equals(m2.getContent()) && m.getTimestamp().compareTo(m2.getTimestamp()) == 0){
//                                    exists = true;
//                                    break;
//                                }
//                            }
//
//                            if(!exists){
//                                userMessages.addAll(messages);
//                                subscribers.forEach(i -> i.update(m));
//                            }
//                        }
//
//                        Thread.sleep(1000);
//                    } catch (InterruptedException | IOException | ParseException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        thread.start();
//    }

    @Override
    public String getName() {
        return serviceName;
    }

    @Override
    public Image getIcon() {
        return serviceIcon;
    }
}
