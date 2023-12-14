package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;
import cs451.MessageBatch;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import cs451.links.udp.UDPReceiver;
import cs451.links.udp.UDPSender;

public class FairLossLink implements Observer {
  private final int SENDER_NUMBER = 3;
  private final ExecutorService senderPool = Executors.newFixedThreadPool(SENDER_NUMBER);
  private final Observer observer;
  private HashMap<Byte, Host> hostMap;
  private Thread receiverThread;
  private DatagramSocket socket;
  private int proposalSetSize;

  FairLossLink(Observer observer, int port, HashMap<Byte, Host> hostMap, int proposalSetSize) {
    this.observer = observer;
    this.proposalSetSize = proposalSetSize;
    try {
      this.socket = new DatagramSocket();
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.hostMap = hostMap;
    this.receiverThread = new Thread(new UDPReceiver(this, port, proposalSetSize));
  }

  void send(MessageBatch message) {
    Host host = hostMap.get(message.getReceiverId());
    senderPool.submit(new UDPSender(host.getIp(), host.getPort(), message, socket, proposalSetSize));
  }

  void start() {
    receiverThread.start();
  }

  public void stop() {
    receiverThread.interrupt();
    senderPool.shutdown();
  }

  @Override
  public void deliver(Message message) {
    observer.deliver(message);
  }
}
