package cs451.links;

import cs451.Observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import cs451.Host;
import cs451.Message;

public class StubbornLink implements Observer {
    private final FairLossLink fl;
    private final Observer observer;
    private final List<ConcurrentHashMap<Integer, Message>> pools;

    public StubbornLink(Observer observer, int port, HashMap<Byte, Host> hostMap) {
        this.fl = new FairLossLink(this, port, hostMap);
        this.observer = observer;
        List<ConcurrentHashMap<Integer, Message>> tmpPools = new ArrayList<>();
        for (int i = 0; i < hostMap.size() + 1; i++) {
            tmpPools.add(new ConcurrentHashMap<>());
        }
        this.pools = Collections.unmodifiableList(tmpPools);
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
                for (ConcurrentHashMap<Integer, Message> pool : pools) {
                    pool.forEach((key, val) -> {
                        fl.send(val);
                    });
                }
            }
        }, 1000, 2000);
    }

    public void send(Message message) {
        if(message.isAck()) {
            fl.send(message);
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
            return;
        }
        observer.deliver(message);
        message.ack();
        send(message);
    }
}