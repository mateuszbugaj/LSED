package Devices;

import Utils.Publisher;
import Utils.Subscriber;

public interface CurrentStatePublisher{
    void addCurrentStateSubscriber(CurrentStateSubscriber subscriber);
    void removeCurrentStateSubscriber(CurrentStateSubscriber subscriber);
}
