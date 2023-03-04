import StreamingService.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MessageManagementTest {

    @Test
    public void addNewMessageTest(){
        // Given
        String messageContent = "Message content ABC";
        String userName = "User1";

        UserManager userManager = new UserManager(List.of());
        ChatManager chatManager = new ChatManager(userManager);

        // When
        chatManager.handleNewMessage(messageContent, userName);

        // Then
        Message message = chatManager.getChatMessages().get(0);

        Assertions.assertEquals(1, chatManager.getChatMessages().size());
        Assertions.assertEquals(messageContent, message.getContent());
        Assertions.assertEquals(userName, message.getUser().getName());
    }

    @Test
    public void setMessageTypeTest(){
        UserManager userManager = new UserManager(List.of());
        ChatManager chatManager = new ChatManager(userManager);

        /* USER MESSAGE */
        // Given
        String userMessageContent = "Message content ABC";
        String userName = "User2";

        // When
        chatManager.handleNewMessage(userMessageContent, userName);

        // Then
        Message message = chatManager.getChatMessages().get(0);
        Assertions.assertEquals(MessageType.MESSAGE, message.getMessageType());
        Assertions.assertEquals(MessageOwnership.USER, message.getMessageOwnership());

        /* USER COMMAND */
        // Given
        userMessageContent = "!Device abc";
        userName = "User3";

        // When
        chatManager.handleNewMessage(userMessageContent, userName);

        // Then
        message = chatManager.getChatMessages().get(1);
        Assertions.assertEquals(MessageType.COMMAND, message.getMessageType());
        Assertions.assertEquals(MessageOwnership.USER, message.getMessageOwnership());

        /* ADMIN MESSAGE */
        // Given
        userMessageContent = "Message content ABC";
        userName = "Admin";

        // When
        chatManager.handleNewMessage(userMessageContent, userName);

        // Then
        message = chatManager.getChatMessages().get(2);
        Assertions.assertEquals(MessageType.MESSAGE, message.getMessageType());
        Assertions.assertEquals(MessageOwnership.ADMIN, message.getMessageOwnership());

        /* ADMIN COMMAND */
        // Given
        userMessageContent = "!Device abc";
        userName = "Admin";

        // When
        chatManager.handleNewMessage(userMessageContent, userName);

        // Then
        message = chatManager.getChatMessages().get(3);
        Assertions.assertEquals(MessageType.COMMAND, message.getMessageType());
        Assertions.assertEquals(MessageOwnership.ADMIN, message.getMessageOwnership());
    }

    @Test
    public void messageSubscriptionTest(){
        // Given
        UserManager userManager = new UserManager(List.of());
        ChatManager chatManager = new ChatManager(userManager);
        String messageContent = "!system abc";
        String userName = "User1";

        chatManager.addMessageSubscriber(new MessageSubscriber() {
            private final String name = "system";
            @Override
            public void annotateMessage(Message message) {
                if(message.getMessageType().equals(MessageType.COMMAND)){
                    if(message.getContent().split(" ")[0].replaceFirst("!", "").equals(name)){
                        message.setType(MessageType.SYSTEM_COMMAND);
                    }
                }
            }

            @Override
            public void handleMessage(Message message) {
                Assertions.assertEquals(messageContent, message.getContent());
            }
        });

        // When
        chatManager.handleNewMessage(messageContent, userName);

        // Then
        Message message = chatManager.getChatMessages().get(0);
        Assertions.assertEquals(MessageType.SYSTEM_COMMAND, message.getMessageType());
    }
}
