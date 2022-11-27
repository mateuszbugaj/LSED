package StreamingService;

import Utils.Subscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ChatManagerTest {

    @Test
    void receiveUserMessageTest() {
        ChatManager chatManager = new ChatManager();

        class UserMessageSubscriberImpl implements Subscriber<UserMessage>{
            ArrayList<UserMessage> userMessages = new ArrayList<>();

            @Override
            public void update(UserMessage content) {
                userMessages.add(content);
            }
        }

        UserMessageSubscriberImpl messageSubscriber = new UserMessageSubscriberImpl();
        chatManager.addSubscriber(messageSubscriber);
        chatManager.update(new UserMessage("User1", "/dev moveBy 10", new Date()));
        chatManager.update(new UserMessage("User1", "dev moveBy 10", new Date()));

        Assertions.assertEquals(MessageType.USER_COMMAND, messageSubscriber.userMessages.get(0).getMessageType());
//        Assertions.assertEquals(MessageType.NONE, messageSubscriber.userMessages.get(1).getMessageType()); // todo
    }
}