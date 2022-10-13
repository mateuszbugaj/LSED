package View;

import Device.Device;

import java.io.IOException;

public class Command{
    private final Device device;

    public Command(Device device) {
        this.device = device;
    }

    public void run(String message) {
        System.out.println("DeviceSendCommand: " + device.getDeviceName() + " <- " + message);

        try {
            device.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
