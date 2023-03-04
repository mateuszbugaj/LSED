import StreamingService.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

public class UserManagementTest {

    @Test
    public void addUserTest() {
        // Given
        UserManager userManager = new UserManager(List.of());
        String userName = "user1";

        // When
        userManager.getUser(userName);
        List<User> users = userManager.getUsers();

        // Then
        Assertions.assertEquals(1, users.size());
        Assertions.assertEquals(userName, users.get(0).getName());
    }

    @Test
    public void addMessageTest(){
        // Given
        UserManager userManager = new UserManager(List.of());
        ChatManager chatManager = new ChatManager(userManager);
        String messageContent = "Message Content ABC";
        String messageUserName = "User1";

        // When
        chatManager.handleNewMessage(messageContent, messageUserName);

        // Then
        Assertions.assertEquals(1, chatManager.getChatMessages().size());
        Assertions.assertEquals(1, userManager.getUsers().size());
        Assertions.assertEquals(messageContent, chatManager.getChatMessages().get(0).getContent());
        Assertions.assertEquals(messageUserName, chatManager.getChatMessages().get(0).getUser().getName());
    }

    @Test
    public void addMultipleMessages(){
        // Given
        UserManager userManager = new UserManager(List.of());
        ChatManager chatManager = new ChatManager(userManager);
        String messageContent = "Message Content ABC";
        String messageUserName1 = "User1";
        String messageUserName2 = "User2";
        String messageUserName3 = "User3";

        // When
        chatManager.handleNewMessage(messageContent, messageUserName1);
        chatManager.handleNewMessage(messageContent, messageUserName2);
        chatManager.handleNewMessage(messageContent, messageUserName2);
        chatManager.handleNewMessage(messageContent, messageUserName3);

        // Then
        Assertions.assertEquals(4, chatManager.getChatMessages().size());
        Assertions.assertEquals(3, userManager.getUsers().size());
    }

    @Test
    public void changeActiveUserAfterRequest(){
        // Given
        UserManager userManager = new UserManager(List.of());
        String newUserName = "User1";
        Message message = new Message(userManager.getUser(newUserName), "!control request 10", new Date());
        message.setType(MessageType.COMMAND);

        // When
        userManager.annotateMessage(message);
        userManager.handleMessage(message);

        // Then
        Assertions.assertEquals(newUserName, userManager.getActiveUser().get().getName());
        Assertions.assertEquals(600, userManager.getActiveUserTimerSeconds().get());
    }

    @Test
    public void userRequestQueueTest(){
        // Given
        UserManager userManager = new UserManager(List.of());

        // When
        userManager.addRequest(userManager.getUser("User1"), 0.1f);
        userManager.addRequest(userManager.getUser("User2"), 0.1f);

        // Then
        Assertions.assertEquals("User1", userManager.getActiveUser().get().getName());

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals("User2", userManager.getActiveUser().get().getName());
    }
}
