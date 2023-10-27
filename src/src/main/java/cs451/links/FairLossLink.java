package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import cs451.links.udp.UDPReceiver;
import cs451.links.udp.UDPSender;

public class FairLossLink implements Observer {
    private final int SENDER_NUMBER = 1;
    private final ExecutorService senderPool = Executors.newFixedThreadPool(SENDER_NUMBER);
    private final Observer observer;
    private HashMap<Byte, Host> hostMap;
    private Thread receiverThread;
    private DatagramSocket socket;

    FairLossLink(Observer observer, int port, HashMap<Byte, Host> hostMap) {
        this.observer = observer;
        try {
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.hostMap = hostMap;
        this.receiverThread = new Thread(new UDPReceiver(this, port));
    }

    void send(Message message) {
        Host host = hostMap.get(message.getReceiverId());
        senderPool.submit(new UDPSender(host.getIp(), host.getPort(), message, socket));
    }

    void start() {
        receiverThread.start();
    }

    public static void stop() {
        UDPReceiver.stopReceiver();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
