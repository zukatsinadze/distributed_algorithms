package cs451.links;

import cs451.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import cs451.Host;
import cs451.Message;
// import cs451.Constants;
import cs451.MessageBatch;

public class StubbornLink implements Observer {
    private final FairLossLink fl;
    private final Observer observer;
    private ConcurrentHashMap<Integer, Message> messagePool = new ConcurrentHashMap<>();
    private final HashMap<Byte, Host> hostMap;

    public StubbornLink(Observer observer, int port, HashMap<Byte, Host> hostMap) {
        this.fl = new FairLossLink(this, port, hostMap);
        this.observer = observer;
        this.hostMap = hostMap;
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

                // List<Message> messages = new ArrayList<>();
                // int batchSize = 0;
                Map<Byte, List<Message>> batches = new HashMap<>();
                for (Byte key : hostMap.keySet()) {
                    batches.put(key, new ArrayList<Message>());
                }

                for (Map.Entry<Integer, Message> entry : messagePool.entrySet()) {
                    Message m = entry.getValue();
                    batches.get(m.getReceiverId()).add(m);
                    if (batches.get(m.getReceiverId()).size() == 8) {
                        fl.send(new MessageBatch(batches.get(m.getReceiverId())));
                        batches.put(m.getReceiverId(), new ArrayList<Message>());
                    }
                }

                for (Byte key : hostMap.keySet()) {
                    if (batches.get(key).size() > 0) {
                        fl.send(new MessageBatch(batches.get(key)));
                        batches.put(key, new ArrayList<Message>());
                    }
                }
            }
        }, 1000, 2000);
    }

    public void send(Message message) {
        if(message.isAck()) {
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            fl.send(new MessageBatch(messages));
            return;
        }
        messagePool.put(message.uniqueId(), message);
    }

    public void stop() {
        fl.stop();
    }

    @Override
    public void deliver(MessageBatch messages) {
        for (Message message : messages.getMessages()) {
            if (message.isAck()) {
                messagePool.remove(message.uniqueId());
                return;
            }

            observer.deliver(message);
            message.ack();
            send(message);
        }
    }

    @Override
    public void deliver(Message m) {

    }
}
