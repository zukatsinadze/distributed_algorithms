package cs451.fifo;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.broadcast.UniformReliableBroadcast;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FIFO implements Observer {
  private final UniformReliableBroadcast urb;
  private final Observer observer;
  private final int[] nextDelivery;
  private final ConcurrentMap<Byte, PriorityQueue<Message>> pending;
  private final int MAGIC_NUMBER;

  private final Lock condLock = new ReentrantLock();
  private final Condition cond = condLock.newCondition();
  private int current = 0;
  private byte myId;

  public FIFO(byte myId, int port, Observer observer,
      HashMap<Byte, Host> hostMap) {
    this.myId = myId;
    if (hostMap.size() <= 9) {
      this.MAGIC_NUMBER = 180000 / (hostMap.size() * hostMap.size());
    } else if (hostMap.size() >= 40) {
      this.MAGIC_NUMBER = 1;
    } else {
      this.MAGIC_NUMBER = 25000 / (hostMap.size() * hostMap.size());
    }
    this.urb = new UniformReliableBroadcast(myId, port, this, hostMap);
    this.observer = observer;

    this.pending = new ConcurrentHashMap<>();
    for (int i = 0; i < hostMap.size(); i++) {
      this.pending.put((byte) i, new PriorityQueue<>());
    }

    this.nextDelivery = new int[hostMap.size()];
    for (int i = 0; i < hostMap.size(); i++) {
      nextDelivery[i] = 1;
    }
  }

  public void broadcast(int messageId, byte originalSenderId) {
    condLock.lock();
    try {
      while (current >= MAGIC_NUMBER) {
        try {
          cond.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } finally {
      current++;
      condLock.unlock();
    }
    urb.broadcast(messageId, originalSenderId);
  }

  public void stop() {
    urb.stop();
  }

  public void start() {
    urb.start();
  }

  public void deliver(Message m) {
    pending.get(m.getOriginalSenderId()).add(m);
    boolean selfDelivery = false;
    if (m.getOriginalSenderId() == myId) {
      selfDelivery = true;
    }

    while (canDeliver(m)) {
      observer.deliver(m);
      nextDelivery[m.getOriginalSenderId()] = m.getMessageId() + 1;
      pending.get(m.getOriginalSenderId()).poll();

      m = pending.get(m.getOriginalSenderId()).peek();
      if (m == null) {
        break;
      }
    }

    if (selfDelivery) {
      condLock.lock();
      try {
        current--;
        cond.signal();
      } finally {
        condLock.unlock();
      }
    }
    System.out.println("Memory usage in mb: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
  }

  private boolean canDeliver(Message m) {
    return m.getMessageId() == nextDelivery[m.getOriginalSenderId()];
  }
}
