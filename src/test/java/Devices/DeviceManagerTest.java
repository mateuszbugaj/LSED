package Devices;

import StreamingService.*;
import Utils.ReturnMessageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

class DeviceManagerTest {

    @Test
    public void deviceManagerReceiveUserCommandTest() throws SerialPortNotFoundException, ReturnMessageException {
        // Given
        ChatManagerMediator chatManagerMediator = new ChatManagerMediator() {
            @Override
            public void handleNewMessage(Message newMessage) {

            }

            @Override
            public void handleNewMessage(String message, String userName) {

            }
        };

        UserManager userManager = new UserManager(List.of());
        DeviceManager deviceManager = new DeviceManager(chatManagerMediator, userManager);

        DeviceCommandParam deviceCommandParam = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "0");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1 desc", "com1", "cm1", List.of(deviceCommandParam), List.of(), List.of("home"), "");
        ExternalDevice externalDevice = new ExternalDevice(
                "dev1",
                new SerialCom(null, 9600),
                List.of(),
                List.of(deviceCommand),
                "home");

        Message message = new Message(userManager.getUser("User1"), "!dev1 com1 10");
        message.setType(MessageType.COMMAND);
        message.setOwnership(MessageOwnership.USER);

        // When
        deviceManager.addDevice(externalDevice);
        deviceManager.annotateMessage(message);
        deviceManager.handleMessage(message);

//        Assertions.assertEquals(MessageType.DEVICE_COMMAND, message.getMessageType());
        Assertions.assertEquals(List.of("cm1 10"), externalDevice.getDeviceCommandsToExecute().pop().getDeviceInstructions());
    }

    @Test
    public void deviceManagerIgnoreCommandForNonExistingDeviceTest() throws SerialPortNotFoundException, ReturnMessageException {
        // Given
        ChatManagerMediator chatManagerMediator = new ChatManagerMediator() {
            @Override
            public void handleNewMessage(Message newMessage) {

            }

            @Override
            public void handleNewMessage(String message, String userName) {

            }
        };

        UserManager userManager = new UserManager(List.of());
        DeviceManager deviceManager = new DeviceManager(chatManagerMediator, userManager);

        Message message = new Message(userManager.getUser("User1"), "!dev1 com1 10");
        message.setType(MessageType.COMMAND);
        message.setOwnership(MessageOwnership.USER);

        // When
        deviceManager.annotateMessage(message);
        deviceManager.handleMessage(message);

        Assertions.assertEquals(MessageType.COMMAND, message.getMessageType());
        Assertions.assertNotEquals(MessageType.DEVICE_COMMAND, message.getMessageType());
    }
}