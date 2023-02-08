package Utils;

import java.util.List;

public class LSEDConfig {
    private List<String> deviceConfigDir;
    private List<String> streamConfigDir;

    public List<String> getDeviceConfigDir() {
        return deviceConfigDir;
    }

    public void setDeviceConfigDir(List<String> deviceConfigDir) {
        this.deviceConfigDir = deviceConfigDir;
    }

    public List<String> getStreamConfigDir() {
        return streamConfigDir;
    }

    public void setStreamConfigDir(List<String> streamConfigDir) {
        this.streamConfigDir = streamConfigDir;
    }
}
