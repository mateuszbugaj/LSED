package Devices;

import Utils.Publisher;
import Utils.Subscriber;

public interface ReceivedMessagesPublisher {
    void addReceivedMessageSubscriber(ReceivedMessagesSubscriber subscriber);
    void removeReceivedMessageSubscriber(ReceivedMessagesSubscriber subscriber);
}
