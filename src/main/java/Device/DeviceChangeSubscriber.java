package Device;

// todo: the immediate problem here is that every DeviceChangeSubscriber needs to have an associated currentSelectedDevice Device field
public interface DeviceChangeSubscriber {
    void update(Device newSelectedDevice);
}
