import Device.Device;
import Device.DeviceManager;
import Device.ReceivedMessage;
import StreamingService.Chat;
import StreamingService.ChatManager;
import StreamingService.UserMessage;
import View.AuxiliaryWindow;
import View.MainWindow;
import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Date;

public class LSEDApplication extends Application {
    public static void main(String[] args) {
        Application.launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("LSED");

        for(SerialPort serialPort:SerialPort.getCommPorts()){
            System.out.println(serialPort.getSystemPortName());
        }

        Device device1 = new Device("src/main/resources/arduino.yaml");
        Device device2 = new Device("src/main/resources/manipulator.yaml");
        DeviceManager deviceManager = new DeviceManager();
        deviceManager.addDevice(device1);
        deviceManager.addDevice(device2);

//        for(int i = 0; i < 5; i++){
//            deviceManager.getDeviceState(0).receivedMessages.add(new ReceivedMessage("xxx", new Date()));
//        }

        Chat twitchChat = new Chat("src/main/resources/twitch.yaml");
        Chat youtubeChat = new Chat("src/main/resources/youtube.yaml");
        ChatManager chatManager = new ChatManager();
        chatManager.addChat(twitchChat);
        chatManager.addChat(youtubeChat);

//        for(int i = 0; i < 5; i++){
//            chatManager.getChatMessages().add(new UserMessage("User1", "XXX", new Date()));
//        }

        MainWindow mainWindow = new MainWindow(stage, deviceManager, chatManager);
        mainWindow.show();

        AuxiliaryWindow auxiliaryWindow = new AuxiliaryWindow(deviceManager, chatManager);
        auxiliaryWindow.show();
    }
}
