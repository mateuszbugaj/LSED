import Devices.*;
import StreamingService.*;
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
import java.util.List;

public class LSEDApplication extends Application {
    public static Logger logger = LoggerFactory.getLogger(LSEDApplication.class);

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("LSED Start");
        Parameters parameters = getParameters();
        stage.setTitle("LSED");

        logger.info("Available device ports: " + Arrays.toString(SerialPort.getCommPorts()));
        logger.info("Available camera ports: " + Webcam.getWebcams().toString());

        WebcamDiscoveryService discoveryService = Webcam.getDiscoveryService();
        discoveryService.stop();
        discoveryService.setEnabled(false);

        UserManager userManager = new UserManager(List.of(), List.of("Admin")); // todo: These lists should be read from the LSED config file
        ChatManager chatManager = new ChatManager(userManager);
        ChatBuilder chatBuilder = new ChatBuilder(chatManager);
        DeviceManager deviceManager = new DeviceManager(chatManager, userManager);

        chatManager.addMessageSubscriber(deviceManager);
        chatManager.addMessageSubscriber(userManager);

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

        for(String streamConfig:lsedConfig.getStreamConfigDir()){
            ChatService chatService = chatBuilder.build(streamConfig);
            chatManager.addChat(chatService);
        }

        MainWindow mainWindow = new MainWindow(stage, deviceManager, chatManager, userManager);
        chatManager.addMessageSubscriber(mainWindow);
        AuxiliaryWindow auxiliaryWindow = new AuxiliaryWindow(deviceManager, chatManager);

//        todo: here to notify all DeviceChangeSubscribers
        if(deviceManager.getDevices().size() > 0){
            deviceManager.changeSelectedDevice(0);
        }

        mainWindow.show();
        auxiliaryWindow.show();
        chatManager.handleNewMessage("Application start", "Admin");
    }
}
