import StreamingService.*;
import Utils.ReturnMessageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

public class UserManagementTest {

    @Test
    public void addUserTest() {
        // Given
        UserManager userManager = new UserManager(List.of(), List.of());
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
        UserManager userManager = new UserManager(List.of(), List.of());
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
        UserManager userManager = new UserManager(List.of(), List.of());
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
    public void changeActiveUserAfterRequest() throws Exception {
        // Given
        UserManager userManager = new UserManager(List.of(), List.of());
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
        UserManager userManager = new UserManager(List.of(), List.of());

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

    @Test
    public void annotateCommandMessageTest(){
        // Given
        UserManager userManager = new UserManager(List.of(), List.of());
        Message requestMessage = new Message(userManager.getUser("User1"), "!control request 5");
        requestMessage.setType(MessageType.COMMAND);

        // When
        userManager.annotateMessage(requestMessage);

        // Then
        Assertions.assertEquals(MessageType.CONTROL_COMMAND, requestMessage.getMessageType());
    }

    @Test
    public void ignoreAnnotationOfNotCommandMessageTest(){
        // Given
        UserManager userManager = new UserManager(List.of(), List.of());
        Message requestMessage = new Message(userManager.getUser("User1"), "!control request 5");
        requestMessage.setType(MessageType.MESSAGE);

        // When
        userManager.annotateMessage(requestMessage);

        // Then
        Assertions.assertEquals(MessageType.MESSAGE, requestMessage.getMessageType());
    }

    @Test
    public void requestErrorMessageTest(){
        // Given
        UserManager userManager = new UserManager(List.of(), List.of());
        ChatManager chatManager = new ChatManager(userManager);
        chatManager.addMessageSubscriber(userManager);
        Message requestMessage = new Message(userManager.getUser("User1"), "!control abc 5");

        // When
        chatManager.handleNewMessage(requestMessage);

        // Then
        Assertions.assertEquals(MessageType.ERROR, chatManager.getChatMessages().get(1).getMessageType());
    }

    @Test
    public void banUserTest(){
        // Given
        UserManager userManager = new UserManager(List.of(), List.of());
        ChatManager chatManager = new ChatManager(userManager);
        chatManager.addMessageSubscriber(userManager);
        String bannedUsername = "User1";
        Message banMessage = new Message(userManager.getUser("Admin"), "!control ban " + bannedUsername);
        Message requestMessage = new Message(userManager.getUser(bannedUsername), "!control request 5");

        // When
        chatManager.handleNewMessage(banMessage);
        chatManager.handleNewMessage(requestMessage);

        // Then
        Assertions.assertEquals(MessageType.CONTROL_COMMAND, chatManager.getChatMessages().get(0).getMessageType());
        Assertions.assertEquals(MessageType.INFO, chatManager.getChatMessages().get(1).getMessageType());
        Assertions.assertEquals(MessageType.CONTROL_COMMAND, chatManager.getChatMessages().get(2).getMessageType());
        Assertions.assertEquals(MessageType.ERROR, chatManager.getChatMessages().get(3).getMessageType());
    }

    @Test
    public void unbanUserTest(){
        // Given
        UserManager userManager = new UserManager(List.of(), List.of());
        ChatManager chatManager = new ChatManager(userManager);
        chatManager.addMessageSubscriber(userManager);
        String bannedUsername = "User1";
        Message banMessage = new Message(userManager.getUser("Admin"), "!control ban " + bannedUsername);
        Message requestMessage = new Message(userManager.getUser(bannedUsername), "!control request 5");
        Message unbanMessage = new Message(userManager.getUser("Admin"), "!control unban " + bannedUsername);

        // When
        chatManager.handleNewMessage(banMessage);
        chatManager.handleNewMessage(requestMessage);
        chatManager.handleNewMessage(unbanMessage);
        chatManager.handleNewMessage(requestMessage);

        // Then
        Assertions.assertEquals(MessageType.CONTROL_COMMAND, chatManager.getChatMessages().get(0).getMessageType());
        Assertions.assertEquals(MessageType.INFO, chatManager.getChatMessages().get(1).getMessageType());
        Assertions.assertEquals(MessageType.CONTROL_COMMAND, chatManager.getChatMessages().get(2).getMessageType());
        Assertions.assertEquals(MessageType.ERROR, chatManager.getChatMessages().get(3).getMessageType());
        Assertions.assertEquals(MessageType.CONTROL_COMMAND, chatManager.getChatMessages().get(4).getMessageType());
        Assertions.assertEquals(MessageType.INFO, chatManager.getChatMessages().get(5).getMessageType());
        Assertions.assertEquals(userManager.getUser(bannedUsername), userManager.getActiveUser().get());
    }

    @Test
    public void requestControlForAnotherUserTest() throws Exception {
        // Given
        String userAdmin = "UserAdmin";
        UserManager userManager = new UserManager(List.of(), List.of(userAdmin));
        String requestTarget = "User1";
        Message message = new Message(userManager.getUser(userAdmin), "!control request 10 " + requestTarget);
        message.setType(MessageType.COMMAND);

        // When
        userManager.annotateMessage(message);
        userManager.handleMessage(message);

        // Then
        Assertions.assertEquals(requestTarget, userManager.getActiveUser().get().getName());
    }

    @Test
    public void requestControlForAnotherUserAsNonAdminTest() throws Exception {
        // Given
        String userNonAdmin = "UserX";
        UserManager userManager = new UserManager(List.of(), List.of());
        String requestTarget = "User1";
        Message message = new Message(userManager.getUser(userNonAdmin), "!control request 10 " + requestTarget);
        message.setType(MessageType.COMMAND);

        // When
        userManager.annotateMessage(message);
        try{
            userManager.handleMessage(message);
        } catch (Exception e){

        }

        // Then
        Assertions.assertNull(userManager.getActiveUser().get());
    }
}
