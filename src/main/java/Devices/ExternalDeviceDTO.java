package Devices;

import View.CameraDTO;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExternalDeviceDTO implements Comparable<ExternalDeviceDTO>{
    private String name;
    private String portName;
    private Integer portBaudRate = 9600;
    private List<CameraDTO> cameras = new ArrayList<>();
    private List<DeviceCommandDTO> commands = new ArrayList<>();
    private String initialState;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public Integer getPortBaudRate() {
        return portBaudRate;
    }

    public void setPortBaudRate(Integer portBaudRate) {
        this.portBaudRate = portBaudRate;
    }

    public List<CameraDTO> getCameras() {
        return cameras;
    }

    public void setCameras(List<CameraDTO> cameras) {
        this.cameras = cameras;
    }

    public List<DeviceCommandDTO> getCommands() {
        return commands;
    }

    public void setCommands(List<DeviceCommandDTO> commands) {
        this.commands = commands;
    }

    public void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    public String getInitialState() {
        return initialState;
    }

    @Override
    public int compareTo(@NotNull ExternalDeviceDTO o) {

        if(!(name.equals(o.getName()) &&
                portName.equals(o.getPortName()) &&
                Objects.equals(portBaudRate, o.getPortBaudRate())
//                Objects.equals(cameras, o.getCameras()))){ // todo: compare cameras (implement compareTo in the camera to not compare obj hash)
                  )){
            return -1;
        }

        if(initialState != null){
            if(o.getInitialState() == null) return -1;
            if(!initialState.equals(o.getInitialState())){
                return -1;
            }
        }

        if(commands.size() != o.getCommands().size()){
            return -1;
        }

        for(int i = 0; i < commands.size(); i++){
            boolean isPresent = false;
            for(int j = 0; j < o.getCommands().size(); j++){
                if(commands.get(i).compareTo(o.getCommands().get(j)) == 0){
                    isPresent = true;
                    break;
                }

            }

            if(!isPresent){
                return -1;
            }
        }

        return 0;
    }
}