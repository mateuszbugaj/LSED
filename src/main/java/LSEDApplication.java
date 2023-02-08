import Devices.*;
import StreamingService.Chat;
import StreamingService.ChatManager;
import StreamingService.MessageType;
import StreamingService.UserMessage;
import Utils.LSEDConfig;
import View.AuxiliaryWindow;
import View.MainWindow;
import com.fazecast.jSerialComm.SerialPort;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryService;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

public class LSEDApplication extends Application {
    public static Logger logger = LoggerFactory.getLogger(LSEDApplication.class);

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parameters parameters = getParameters();
        logger.info("LSED Start");
        stage.setTitle("LSED");

        logger.info("Available device ports: " + Arrays.toString(SerialPort.getCommPorts()));
        logger.info("Available camera ports: " + Webcam.getWebcams().toString());
        WebcamDiscoveryService discoveryService = Webcam.getDiscoveryService();
        discoveryService.stop();
        discoveryService.setEnabled(false);

        DeviceManager deviceManager = new DeviceManager();

        Yaml yaml = new Yaml(new Constructor(LSEDConfig.class));
        InputStream inputStream = new FileInputStream(parameters.getRaw().get(0));
        LSEDConfig lsedConfig = yaml.load(inputStream);

        ExternalDeviceBuilder externalDeviceBuilder = new ExternalDeviceBuilder();
        ExternalDeviceBuilderDirector builderDirector = new ExternalDeviceBuilderDirector(externalDeviceBuilder);

        for(String deviceConfig:lsedConfig.getDeviceConfigDir()){
            try {
                ExternalDevice device = builderDirector.build(deviceConfig);
                deviceManager.addDevice(device);
            } catch (SerialPortNotFoundException e) {
                logger.error(e.toString());
            }
        }

        ChatManager chatManager = new ChatManager();
        for(String streamConfig:lsedConfig.getStreamConfigDir()){
            Chat chat = new Chat(streamConfig);
            chatManager.addChat(chat);
        }

        chatManager.update(new UserMessage("Admin", "Application start", new Date()).setMessageType(MessageType.ADMIN_MESSAGE));

        MainWindow mainWindow = new MainWindow(stage, deviceManager, chatManager);
        mainWindow.show();

        AuxiliaryWindow auxiliaryWindow = new AuxiliaryWindow(deviceManager, chatManager);
        auxiliaryWindow.addSubscriber(chatManager);

        deviceManager.addSubscriber(chatManager);
        chatManager.addSubscriber(deviceManager); // todo: this should subscribe to the UserCommand notifications

//        todo: here to notify all DeviceChangeSubscribers
        if(deviceManager.getDevices().size() > 0){
            deviceManager.changeSelectedDevice(0);
        }

        auxiliaryWindow.show();
    }
}
