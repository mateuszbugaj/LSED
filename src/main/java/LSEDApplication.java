import Devices.*;
import StreamingService.*;
import Utils.LSEDConfig;
import Utils.LSEDConfigDto;
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

public class LSEDApplication extends Application {
    public static Logger logger = LoggerFactory.getLogger(LSEDApplication.class);

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parameters parameters = getParameters();
        String lsedConfigFilePath = parameters.getRaw().get(0);

        Yaml yaml = new Yaml(new Constructor(LSEDConfigDto.class));
        InputStream inputStream = new FileInputStream(lsedConfigFilePath);
        LSEDConfig.load(yaml.load(inputStream));

        stage.setTitle("LSED");

        logger.info("Available device ports: " + Arrays.toString(SerialPort.getCommPorts()));
        logger.info("Available camera ports: " + Webcam.getWebcams().toString());

        WebcamDiscoveryService discoveryService = Webcam.getDiscoveryService();
        discoveryService.stop();
        discoveryService.setEnabled(false);

        UserManager userManager = new UserManager(LSEDConfig.get().getAdminUsers());
        ChatManager chatManager = new ChatManager(userManager);
        ChatBuilder chatBuilder = new ChatBuilder(chatManager);
        DeviceManager deviceManager = new DeviceManager(chatManager, userManager);

        chatManager.addMessageSubscriber(deviceManager);
        chatManager.addMessageSubscriber(userManager);

        ExternalDeviceBuilder externalDeviceBuilder = new ExternalDeviceBuilder();
        ExternalDeviceBuilderDirector builderDirector = new ExternalDeviceBuilderDirector(externalDeviceBuilder);

        if(LSEDConfig.get().getDeviceConfigDir() != null && !LSEDConfig.get().getDeviceConfigDir().isEmpty()){
            for(String deviceConfig:LSEDConfig.get().getDeviceConfigDir()){
                try {
                    if(deviceConfig == null || deviceConfig.isEmpty()) continue;
                    ExternalDevice device = builderDirector.build(deviceConfig);
                    deviceManager.addDevice(device);
                } catch (SerialPortNotFoundException e) {
                    logger.error(e.toString());
                }
            }
        } else {
            logger.error("Device list 'deviceConfigDir' is not present in LSEDConfig file");
        }

        if(LSEDConfig.get().getStreamConfigDir() != null && !LSEDConfig.get().getStreamConfigDir().isEmpty()){
            for(String streamConfig:LSEDConfig.get().getStreamConfigDir()){
                if(streamConfig == null || streamConfig.isEmpty()) continue;
                ChatService chatService = chatBuilder.build(streamConfig);
                chatManager.addChat(chatService);
            }
        } else {
            logger.error("Device list 'streamConfigDir' is not present in LSEDConfig file");
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
        chatManager.handleNewMessage(new Message(userManager.getUser("Admin"), "Application start").setType(MessageType.INFO));
    }
}