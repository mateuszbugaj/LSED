package View;

import Devices.ExternalDevice;

import java.io.IOException;

public class Command{
    private final ExternalDevice device;

    public Command(ExternalDevice device) {
        this.device = device;
    }

    public void run(String message) {
        System.out.println("DeviceSendCommand: " + device.getName() + " <- " + message);

        try {
            device.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
