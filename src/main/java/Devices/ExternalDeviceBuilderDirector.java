package Devices;

import Interpreter.Interpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

public class ExternalDeviceBuilderDirector {
    private static Logger logger = LoggerFactory.getLogger(ExternalDeviceBuilderDirector.class);
    private final ExternalDeviceBuilder builder;

    public ExternalDeviceBuilderDirector(ExternalDeviceBuilder builder) {
        this.builder = builder;
    }

    public ExternalDevice build(String configFile) throws FileNotFoundException, SerialPortNotFoundException {
        Yaml yaml = new Yaml(new Constructor(ExternalDeviceDTO.class));
        InputStream inputStream = new FileInputStream(configFile);

        ExternalDeviceDTO deviceDTO = yaml.load(inputStream);
        return build(deviceDTO);
    }

    public ExternalDevice build(ExternalDeviceDTO deviceDTO) throws SerialPortNotFoundException { // todo: think how to better present error in the building process
        return builder
                .setDeviceName(deviceDTO.getName())
                .setSerialCom(deviceDTO.getPortName(), deviceDTO.getPortBaudRate())
                .setCameras(deviceDTO.getCameras())
                .setCommands(deviceDTO.getCommands())
                .setInitialState(deviceDTO.getInitialState())
                .setConfirmation(deviceDTO.getConfirmation())
                .build();
    }
}