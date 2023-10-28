package cs451.links;

import cs451.Observer;

import java.util.HashMap;
import java.util.Map;
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
                for (Map.Entry<Integer, Message> entry : messagePool.entrySet()) {
                    fl.send(entry.getValue());
                }
            }
        }, 1000, 2000);
    }

    public void send(Message message) {
        if(message.isAck()) {
            fl.send(message);
            return;
        }

        // System.out.println("Memory usage in mb: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) );
        // if memory usage is bigger than 60 mb start garbage collection
        // if (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 60 * 1024 * 1024) {
            // System.gc();
        // }
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
            System.out.println("Recieved ack, removing from pool, pool size: " + messagePool.size());
            message.ack_ack();
            send(message);
            return;
        }

        observer.deliver(message);
        message.ack();
        send(message);
    }
}
