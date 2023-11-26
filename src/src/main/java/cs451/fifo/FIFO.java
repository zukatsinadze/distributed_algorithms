package cs451.fifo;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.broadcast.UniformReliableBroadcast;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;


public class FIFO implements Observer {
  private final UniformReliableBroadcast urb;
  private final Observer observer;
  private final int[] nextDelivery;
  private final ConcurrentMap<Byte, PriorityQueue<Message>> pending;
  private final int MAGIC_NUMBER;
  private AtomicInteger current = new AtomicInteger(0);
  private byte myId;

  public FIFO(byte myId, int port, Observer observer,
              HashMap<Byte, Host> hostMap) {
    this.myId = myId;
    this.MAGIC_NUMBER =  160000 / (hostMap.size() * hostMap.size());;
    this.urb = new UniformReliableBroadcast(myId, port, this, hostMap);
    this.observer = observer;

    this.pending = new ConcurrentHashMap<>();
    for (int i = 0; i < hostMap.size(); i++) {
      this.pending.put((byte)i, new PriorityQueue<>());
    }

    this.nextDelivery = new int[hostMap.size()];
    for (int i = 0; i < hostMap.size(); i++) {
      nextDelivery[i] = 1;
    }
  }

  public void broadcast(int messageId, byte originalSenderId) {
    while (current.get() >= MAGIC_NUMBER) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    current.incrementAndGet();
    urb.broadcast(messageId, originalSenderId);
  }

  public void stop() { urb.stop(); }

  public void start() { urb.start(); }

  public void deliver(Message m) {
    pending.get(m.getOriginalSenderId()).add(m);

    if (m.getOriginalSenderId() == myId) {
      current.decrementAndGet();
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
  }

  private boolean canDeliver(Message m) {
    return m.getMessageId() == nextDelivery[m.getOriginalSenderId()];
  }
}
