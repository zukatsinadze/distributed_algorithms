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

  public StubbornLink(Observer observer, byte myId, int port, HashMap<Byte, Host> hostMap, int proposalSetSize) {
    this.hostMap = hostMap;
    this.fl = new FairLossLink(this, port, hostMap, proposalSetSize);
    this.observer = observer;
    List<ConcurrentHashMap<Integer, Message>> tmpPools = new ArrayList<>();
    for (int i = 0; i < hostMap.size() + 1; i++) {
      tmpPools.add(new ConcurrentHashMap<>());
    }
    this.pools = Collections.unmodifiableList(tmpPools);

    List<ConcurrentLinkedQueue<Message>> tmpRetry = new ArrayList<>();
    for (int i = 0; i < hostMap.size() + 1; i++) {
      tmpRetry.add(new ConcurrentLinkedQueue<>());
    }
    this.retry = Collections.unmodifiableList(tmpRetry);
  }

  public void clearPools() {
    for (ConcurrentHashMap<Integer, Message> pool : pools) {
      pool.clear();
    }
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
          pool.forEach((key, message) -> {
            // fl.send(val);
            batch.add(message);
            if (message.isAckOrNAck()) {
              pool.remove(key);
            }
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
    }, 0, 1500); // TODO: Massive
  }

  public void send(Message message) {
    // fl.send(message);
    // if (message.isAckOrNAck()) {
    //   fl.send(new MessageBatch(message));
    //   // fl.send(message);
    //   return;
    // }

    int windowSize = 300000 / (this.hostMap.size() * this.hostMap.size());
    if (pools.get(message.getReceiverId()).size() >= windowSize) {
      retry.get(message.getReceiverId()).add(message);
      return;
    }
    pools.get(message.getReceiverId()).put(message.uniqueId(), message);
  }

  public void stop() {
    fl.stop();
    clearPools();
  }

  @Override
  public void deliver(Message message) {
    // System.out.println("Delivered " + message.getMessageId());
    if (message.isAckOrNAck()) {
      pools.get(message.getSenderId()).remove(message.uniqueId());
      // if (pools.get(message.getSenderId()).remove(message.uniqueId()) != null)
        observer.deliver(message);
      Message retryMessage = retry.get(message.getSenderId()).poll();
      if (retryMessage != null) {
        pools.get(message.getSenderId()).put(retryMessage.uniqueId(), retryMessage);
      }
    } else {
      observer.deliver(message);
    }
  }
}
