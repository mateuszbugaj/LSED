package StreamingService;

import Utils.Subscriber;
import javafx.scene.image.Image;

import java.util.ArrayList;

public interface ChatService {
    void addNewMessageSubscription(ArrayList<Subscriber<UserMessage>> subscribers);
    void sendMessage(String message);
    Image getIcon();
}
