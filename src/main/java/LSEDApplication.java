import Devices.*;
import StreamingService.Chat;
import StreamingService.ChatManager;
import StreamingService.MessageType;
import StreamingService.UserMessage;
import View.AuxiliaryWindow;
import View.MainWindow;
import com.fazecast.jSerialComm.SerialPort;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryService;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class LSEDApplication extends Application {
    public static Logger logger = LoggerFactory.getLogger(LSEDApplication.class);

    public static void main(String[] args) {
        Application.launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("LSED Start");
        stage.setTitle("LSED");

        logger.info("Available device ports: " + Arrays.toString(SerialPort.getCommPorts()));
        logger.info("Available camera ports: " + Webcam.getWebcams().toString());
        WebcamDiscoveryService discoveryService = Webcam.getDiscoveryService();
        discoveryService.stop();
        discoveryService.setEnabled(false);

        DeviceManager deviceManager = new DeviceManager();

        ExternalDeviceBuilder externalDeviceBuilder = new ExternalDeviceBuilder();
        ExternalDeviceBuilderDirector builderDirector = new ExternalDeviceBuilderDirector(externalDeviceBuilder);

        //todo: make better way of deciding on not including specific device than try and catch block
        try {
            ExternalDevice device2 = builderDirector.build("src/main/resources/manipulator.yaml");
            deviceManager.addDevice(device2);
        } catch (SerialPortNotFoundException e) {
            logger.error(e.toString());
        }

        try {
            ExternalDevice device1 = builderDirector.build("src/main/resources/microscope.yaml");
            deviceManager.addDevice(device1);
        } catch (SerialPortNotFoundException e) {
            logger.error(e.toString());
        }

//        Chat twitchChat = new Chat("src/main/resources/twitch.yaml");
//        Chat youtubeChat = new Chat("src/main/resources/youtube.yaml");
        ChatManager chatManager = new ChatManager();
//        chatManager.addChat(twitchChat);
//        chatManager.addChat(youtubeChat);

        chatManager.update(new UserMessage("Admin", "Application start", new Date()).setMessageType(MessageType.ADMIN_MESSAGE));

        MainWindow mainWindow = new MainWindow(stage, deviceManager, chatManager);
        mainWindow.show();

        AuxiliaryWindow auxiliaryWindow = new AuxiliaryWindow(deviceManager, chatManager);
        auxiliaryWindow.addSubscriber(chatManager);

        deviceManager.addSubscriber(chatManager);
        chatManager.addSubscriber(deviceManager); // todo: this should subscribe to the UserCommand notifications

        //todo: here to notify all DeviceChangeSubscribers
        if(deviceManager.getDevices().size() > 0){
            deviceManager.changeSelectedDevice(0);
        }

        auxiliaryWindow.show();
    }
}
