package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;
import java.util.HashMap;
import java.util.HashSet;


public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final Observer observer;
    private final byte myId;
    private HashSet<Integer> deliveredMessages;
    private HashMap<Byte, Host> hostMap;

    public PerfectLink(byte myId, int port, Observer observer, HashMap<Byte, Host> hostMap) {
        this.myId = myId;
        this.hostMap = hostMap;
        this.stubbornLink = new StubbornLink(this, myId, port, hostMap);
        this.observer = observer;
        this.deliveredMessages = new HashSet<>();
    }

    public void send(Message message) {
      // if (message.getOriginalSenderId() != myId) {
      //   stubbornLink.send(message);
      //   return;
      // }

      // while (stubbornLink.getMessagePoolSize(message.getReceiverId()) > 300000 / (this.hostMap.size() * this.hostMap.size())) {
      //   try {
      //     Thread.sleep(100);
      //   } catch (InterruptedException e) {
      //     e.printStackTrace();
      //   }
      // }
      stubbornLink.send(message);
    }

    public void start() {
        stubbornLink.start();
    }

    public void stop() {
        stubbornLink.stop();
    }

    @Override
    public void deliver(Message message) {
        if (deliveredMessages.add(message.uniqueId()))
            observer.deliver(message);
    }
}
