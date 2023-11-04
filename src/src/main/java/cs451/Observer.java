package cs451;

public interface Observer {
    void deliver(MessageBatch messages);
    void deliver(Message message);
}
