package Utils;

public interface Publisher<T> {
    void addSubscriber(Subscriber<T> subscriber);
    void removeSubscriber(Subscriber<T> subscriber);
}
