package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;
import cs451.links.udp.UDPReceiver;
import cs451.links.udp.UDPSender;

public class FairLossLink implements Observer {
    private final UDPReceiver receiver;
    private final Observer observer;

    private Thread receiverThread;
    private Thread senderThread;

    FairLossLink(int port, Observer observer) {
        this.receiver = new UDPReceiver(port, this);
        this.observer = observer;
        receiverThread = new Thread(new UDPReceiver(port, this));
    }

    void send(Message message, Host host) {
        senderThread = new Thread(new UDPSender(host.getIp(), host.getPort(), message));
        senderThread.start();
    }

    void start() {
        receiverThread.start();
    }

    void stop() {
        receiver.stopReceiver();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
