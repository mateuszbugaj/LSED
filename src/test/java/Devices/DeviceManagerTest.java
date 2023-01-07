package Devices;

import StreamingService.UserMessage;
import Utils.Subscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class DeviceManagerTest {

    @Test
    public void deviceManagerReceiveUserCommandTest() throws SerialPortNotFoundException {
        DeviceManager deviceManager = new DeviceManager();

        DeviceCommandParam deviceCommandParam = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "0");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1 desc", "com1", "cm1", List.of(deviceCommandParam), List.of(), List.of("home"), "");
        ExternalDevice externalDevice = new ExternalDevice(
                "dev1",
                new SerialCom(null, 9600),
                List.of(),
                List.of(deviceCommand),
                "home");

        deviceManager.addDevice(externalDevice);
        deviceManager.update(new UserMessage("User1", "!dev1 com1 10", new Date()));
        deviceManager.update(new UserMessage("User1", "!1 com1 10", new Date()));

        Assertions.assertEquals(List.of("cm1 10"), externalDevice.getDeviceCommandsToExecute().pop().getDeviceInstructions());
        Assertions.assertEquals(List.of("cm1 10"), externalDevice.getDeviceCommandsToExecute().pop().getDeviceInstructions());

    }


    @Test
    public void deviceManagerReceiveUserCommandAndSendErrorMessageToTheUserMessageSubscribers() throws SerialPortNotFoundException {
        DeviceManager deviceManager = new DeviceManager();

        class UserMessageSubscriberImpl implements Subscriber<UserMessage>{
            final List<UserMessage> userMessages = new ArrayList<>();

            @Override
            public void update(UserMessage content) {
                userMessages.add(content);
            }
        }

        UserMessageSubscriberImpl messageSubscriber = new UserMessageSubscriberImpl();
        deviceManager.addSubscriber(messageSubscriber);

        DeviceCommandParam deviceCommandParam = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "0");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1 desc", "com1", "cm1", List.of(deviceCommandParam), List.of(), List.of(), "");
        ExternalDevice externalDevice = new ExternalDevice(
                "dev1",
                new SerialCom(null, 9600),
                List.of(),
                List.of(deviceCommand),
                "home");

        deviceManager.addDevice(externalDevice);
        deviceManager.update(new UserMessage("User1", "!dev2 com1 10", new Date()));
        deviceManager.update(new UserMessage("User1", "!dev1 com2 10", new Date()));

        Assertions.assertEquals("Interpreter", messageSubscriber.userMessages.get(0).getUser());
        Assertions.assertEquals("Device dev2 not found", messageSubscriber.userMessages.get(0).getContent());

        Assertions.assertEquals("Interpreter", messageSubscriber.userMessages.get(1).getUser());
        Assertions.assertEquals("No matching commands found in the associated device", messageSubscriber.userMessages.get(1).getContent());
    }

}