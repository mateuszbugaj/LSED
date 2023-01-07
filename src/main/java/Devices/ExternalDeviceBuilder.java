package Devices;

import Interpreter.Interpreter;
import View.Camera;
import View.CameraDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExternalDeviceBuilder {
    private static Logger logger = LoggerFactory.getLogger(ExternalDeviceBuilder.class);

    private String deviceName;
    private SerialCom serialCom;
    private ArrayList<Camera> cameras = new ArrayList<>();
    private final ArrayList<Camera> usedCameras = new ArrayList<>();
    private ArrayList<DeviceCommand> commands = new ArrayList<>();
//    private Integer timeoutTimer = 100; // sec
//    private Thread deviceThread;
    private String initialState;

    public ExternalDeviceBuilder setDeviceName(String deviceName){
        this.deviceName = deviceName;
        return this;
    }

    public ExternalDeviceBuilder setSerialCom(String portName, Integer portBaudRate) throws SerialPortNotFoundException {
        serialCom = new SerialCom(portName, portBaudRate);

        return this;
    }

    public ExternalDeviceBuilder setCameras(List<CameraDTO> cameraDTOS){
        if(cameraDTOS == null) return this;

        for(CameraDTO cameraDTO: cameraDTOS){
            usedCameras
                    .stream()
                    .filter(i -> i.getPortName().equals(cameraDTO.getPortName()))
                    .findFirst()
                    .ifPresentOrElse(c -> {
                        cameras.add(c);
                    }, () -> {
                        Camera camera = new Camera(cameraDTO.getName(), cameraDTO.getPortName());
                        camera.start();
                        usedCameras.add(camera);
                        cameras.add(camera);
                    });
        }

        return this;
    }

    public ExternalDeviceBuilder setCommands(List<DeviceCommandDTO> deviceCommandDTOS){
        for(DeviceCommandDTO deviceCommandDTO : deviceCommandDTOS){
            DeviceCommand deviceCommand = Interpreter.buildCommand(deviceCommandDTO);
            //todo: check if it isn't a duplicate
            commands.add(deviceCommand);
        }

        return this;
    }

    public ExternalDeviceBuilder setInitialState(String state){
        this.initialState = state;

        return this;
    }

    public ExternalDevice build(){
        ExternalDevice device = new ExternalDevice(deviceName, serialCom, cameras, commands, initialState);
        deviceName = null;
        serialCom = null;
        cameras = new ArrayList<>();
        commands = new ArrayList<>();
        initialState = null;

        return device;
    }


}
