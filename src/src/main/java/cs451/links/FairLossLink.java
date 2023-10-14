package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;
import java.net.DatagramSocket;
import java.util.HashMap;

import cs451.links.udp.UDPReceiver;
import cs451.links.udp.UDPSender;

public class FairLossLink implements Observer {
    private final Observer observer;
    private DatagramSocket socket;
    private HashMap<Integer, Host> hostMap;
    private Thread receiverThread;
    private Thread senderThread;

    FairLossLink(Observer observer, DatagramSocket socket, HashMap<Integer, Host> hostMap) {
        this.observer = observer;
        this.socket = socket;
        this.hostMap = hostMap;
        receiverThread = new Thread(new UDPReceiver(this, socket));
    }

    void send(Message message) {
        Host host = hostMap.get(message.getReceiverId());
        senderThread = new Thread(new UDPSender(host.getIp(), host.getPort(), message, this.socket));
        senderThread.start();
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
