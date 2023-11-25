package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UniformReliableBroadcast implements Observer {
  // Threads used: Main, Signal Handler, UDP Receiver, UDP Sender Pool 4,
  // SendPool 1

  private final byte myId;
  private final BestEffortBroadcast beb;
  private final Observer observer;
  private final HashMap<Byte, Host> hostMap;
  private final ExecutorService sendPool = Executors.newFixedThreadPool(1);

  private HashSet<Integer> delivered = new HashSet<>();
  private ConcurrentHashMap<Integer, int[]> pending = new ConcurrentHashMap<>();

  public UniformReliableBroadcast(byte myId, int port, Observer observer,
                                  HashMap<Byte, Host> hostMap) {
    this.hostMap = hostMap;
    this.myId = myId;
    this.beb = new BestEffortBroadcast(port, this, hostMap);
    this.observer = observer;
  }

  @Override
  public void deliver(Message message) {
    Integer msgUniqueId = message.uniqueMessageOriginalSenderId();
    boolean broadcast = false;
    if (pending.get(msgUniqueId) == null) {
      pending.put(msgUniqueId, new int[] {message.getMessageId(),
                                          message.getOriginalSenderId(), 2});
      broadcast = true;
    } else {
      pending.get(msgUniqueId)[2]++;
    }

    if (broadcast) {
      sendPool.submit(new Runnable() {
        @Override
        public void run() {
          beb.broadcast(message.getMessageId(), myId,
                        message.getOriginalSenderId());
        }
      });
    }

    if (pending.get(msgUniqueId)[2] >= (hostMap.size() / 2) + 1 &&
        delivered.add(msgUniqueId)) {
      observer.deliver(message);
      // observer.deliver(new Message(message.getMessageId(),
      // message.getOriginalSenderId(), myId, message.getOriginalSenderId()));
    }
  }

  public void broadcast(int messageId, byte originalSenderId) {
    int[] msg_array = new int[] {messageId, originalSenderId, 1};

    pending.put(Objects.hash(messageId, originalSenderId), msg_array);

    sendPool.submit(new Runnable() {
      @Override
      public void run() {
        beb.broadcast(messageId, myId, originalSenderId);
      }
    });
  }

  public void start() { this.beb.start(); }

  public void stop() { beb.stop(); }
}
