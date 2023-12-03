package cs451.links;

import cs451.Observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs451.Host;
import cs451.Message;
import cs451.MessageBatch;

public class StubbornLink implements Observer {
    private final FairLossLink fl;
    private final Observer observer;
    private final HashMap<Byte, Host> hostMap;
    private final List<ConcurrentHashMap<Integer, Message>> pools;
    private final List<ConcurrentLinkedQueue<Message>> retry;

    public StubbornLink(Observer observer, byte myId, int port, HashMap<Byte, Host> hostMap) {
        this.hostMap = hostMap;
        this.fl = new FairLossLink(this, port, hostMap);
        this.observer = observer;
        List<ConcurrentHashMap<Integer, Message>> tmpPools = new ArrayList<>();
        for (int i = 0; i < hostMap.size() + 1; i++) {
            tmpPools.add(new ConcurrentHashMap<>());
        }
        this.pools = Collections.unmodifiableList(tmpPools);

        List<ConcurrentLinkedQueue< Message>>tmpRetry = new ArrayList<>();
        for (int i = 0; i < hostMap.size() + 1; i++) {
          tmpRetry.add(new ConcurrentLinkedQueue<>());
        }
        this.retry = Collections.unmodifiableList(tmpRetry);
    }

    public void start() {
        fl.start();
        consumePool();
    }

    public int getMessagePoolSize(int id) {
        return pools.get(id).size();
    }

    private void consumePool() {
        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                ArrayList<Message> batch = new ArrayList<>();
                for (ConcurrentHashMap<Integer, Message> pool : pools) {
                    pool.forEach((key, val) -> {
                        batch.add(val);
                        if (batch.size() == 8) {
                            fl.send(new MessageBatch(batch));
                            batch.clear();
                        }
                    });
                    if (batch.size() > 0) {
                        fl.send(new MessageBatch(batch));
                    }
                    batch.clear();
                }
            }
        }, 1000, 2000);
    }

    public void send(Message message) {
        if(message.isAck()) {
            fl.send(new MessageBatch(message));
            return;
        }

        int windowSize = 300000 / this.hostMap.size();
        if (pools.get(message.getReceiverId()).size() >= windowSize) {
            retry.get(message.getReceiverId()).add(message);
            return;
        }
        pools.get(message.getReceiverId()).put(message.uniqueId(), message);
    }

    public void stop() {
        fl.stop();
    }

    @Override
    public void deliver(Message message) {
        if (message.isAck()) {
            pools.get(message.getSenderId()).remove(message.uniqueId());
            Message retryMessage = retry.get(message.getSenderId()).poll();
            if (retryMessage == null) {
                return;
            }
            pools.get(message.getSenderId()).put(retryMessage.uniqueId(), retryMessage);
            return;
        }
        message.ack();
        send(message);
        observer.deliver(message);
    }
}
