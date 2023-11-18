package cs451.links;

import cs451.Observer;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import cs451.Host;
import cs451.Message;

public class StubbornLink implements Observer {
    private final FairLossLink fl;
    private final Observer observer;
    private ConcurrentHashMap<Integer, Message> messagePool = new ConcurrentHashMap<>();

    public StubbornLink(Observer observer, int port, HashMap<Byte, Host> hostMap) {
        this.fl = new FairLossLink(this, port, hostMap);
        this.observer = observer;
    }

    public void start() {
        fl.start();
        consumePool();
    }

    public int getMessagePoolSize() {
        return messagePool.size();
    }

    private void consumePool() {
        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                messagePool.forEach((key, val) -> {
                    fl.send(val);
                });
            }
        }, 1000, 2000);
    }

    public void send(Message message) {
        if(message.isAck()) {
            fl.send(message);
            return;
        }
        messagePool.put(message.uniqueId(), message);
    }

    public static void stop() {
        FairLossLink.stop();
    }

    @Override
    public void deliver(Message message) {
        if (message.isAckAck()) {
            observer.deliver(message);
            return;
        }
        if (message.isAck()) {
            messagePool.remove(message.uniqueId());
            message.ack_ack();
            send(message);
            return;
        }

        observer.deliver(message);
        message.ack();
        send(message);
    }
}
