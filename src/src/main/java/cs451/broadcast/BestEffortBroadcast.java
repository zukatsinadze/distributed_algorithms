package cs451.broadcast;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.PerfectLink;
import java.util.HashMap;

public class BestEffortBroadcast implements Observer {
  private final PerfectLink perfectLink;
  private final Observer observer;
  private final HashMap<Byte, Host> hostMap;

  public BestEffortBroadcast(byte myId, int port, Observer observer,
                             HashMap<Byte, Host> hostMap) {
    this.hostMap = hostMap;
    this.perfectLink = new PerfectLink(myId, port, this, hostMap);
    this.observer = observer;
  }

  @Override
  public void deliver(Message message) {
    observer.deliver(message);
  }

  public void broadcast(int messageId, byte srcId, byte originalSenderId) {
    for (byte key : hostMap.keySet()) {
      if (key != srcId) {
        perfectLink.send(new Message(messageId, srcId, key, originalSenderId));
      }
    }
  }

  public void start() { this.perfectLink.start(); }

  public void stop() { perfectLink.stop(); }
}
