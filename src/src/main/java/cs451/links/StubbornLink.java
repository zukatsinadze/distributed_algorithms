package cs451.links;

import cs451.Observer;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import cs451.Host;
import cs451.Message;
// import cs451.Constants;

public class StubbornLink implements Observer {
    private final FairLossLink fl;
    private final Observer observer;
    private ConcurrentHashMap<Integer, Message> messagePool = new ConcurrentHashMap<>();

    public StubbornLink(Observer observer, DatagramSocket socket, HashMap<Integer, Host> hostMap) {
        this.fl = new FairLossLink(this, socket, hostMap);
        this.observer = observer;
    }

    public void start() {
        fl.start();
        consumePool();
    }

    private void consumePool() {
        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<Integer, Message> entry : messagePool.entrySet()) {
                    fl.send(entry.getValue());
                }
            }
        }, 100, 200);
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
        if (message.isAck()) {
            messagePool.remove(message.uniqueId());
            System.out.println("Recieved ack, removing from pool, pool size: " + messagePool.size());
            return;
        }
        observer.deliver(message);
        send(new Message(message));
    }
}
