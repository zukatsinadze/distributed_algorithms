package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class UniformReliableBroadcast implements Observer {
  private final byte myId;
  private final BestEffortBroadcast beb;
  private final Observer observer;
  private final HashMap<Byte, Host> hostMap;
  private HashSet<Integer> delivered = new HashSet<>();
  private ConcurrentHashMap<Integer, int[]> pending = new ConcurrentHashMap<>();
  private ExecutorService broadcaster = java.util.concurrent.Executors.newFixedThreadPool(1);

  public UniformReliableBroadcast(byte myId, int port, Observer observer,
                                  HashMap<Byte, Host> hostMap) {
    this.hostMap = hostMap;
    this.myId = myId;
    this.beb = new BestEffortBroadcast(myId, port, this, hostMap);
    this.observer = observer;
  }

  @Override
  public void deliver(Message message) {
    Integer msgUniqueId = message.uniqueMessageOriginalSenderId();
    if (pending.get(msgUniqueId) == null) {
      pending.put(msgUniqueId, new int[] {message.getMessageId(),
                                          message.getOriginalSenderId(), 2});
      broadcaster.submit(() -> beb.broadcast(message.getMessageId(), myId, message.getOriginalSenderId()));
    } else {
      pending.get(msgUniqueId)[2]++;
    }

    if (pending.get(msgUniqueId)[2] >= (hostMap.size() / 2) + 1 &&
        delivered.add(msgUniqueId)) {
      observer.deliver(message);
    }
  }

  public void broadcast(int messageId, byte originalSenderId) {
    int[] msg_array = new int[] {messageId, originalSenderId, 1};
    pending.put(Objects.hash(messageId, originalSenderId), msg_array);
    broadcaster.submit(() -> beb.broadcast(messageId, myId, originalSenderId));
  }

  public void start() { this.beb.start(); }

  public void stop() {
    beb.stop();
  }
}
