package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;
import java.net.DatagramSocket;
import cs451.links.udp.UDPReceiver;
import cs451.links.udp.UDPSender;

public class FairLossLink implements Observer {
    private final Observer observer;
    private DatagramSocket socket;

    private Thread receiverThread;
    private Thread senderThread;

    FairLossLink(Observer observer, DatagramSocket socket) {
        this.observer = observer;
        this.socket = socket;
        receiverThread = new Thread(new UDPReceiver(this, socket));
    }

    void send(Message message, Host host) {
        senderThread = new Thread(new UDPSender(host.getIp(), host.getPort(), message, this.socket));
        senderThread.start();
    }

    void start() {
        receiverThread.start();
    }

    public static void stop() {
        UDPReceiver.stopReceiver();
        // receiver.stopReceiver();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
