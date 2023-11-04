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
    // Threads used: Main, Signal Handler, Log Dumper, UDP Receiver, Consume Pool
    private final int SENDER_NUMBER = 3;
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

    void send(MessageBatch messages) {
        Host host = hostMap.get(messages.getReceiverId());
        senderPool.submit(new UDPSender(host.getIp(), host.getPort(), messages, socket));
    }

    void start() {
        receiverThread.start();
    }

    public void stop() {
        UDPReceiver.stopReceiver();
    }

    @Override
    public void deliver(MessageBatch messages) {
        observer.deliver(messages);
    }

    @Override
    public void deliver(Message message) {
    }
}
