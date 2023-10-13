package cs451;

import cs451.links.PerfectLink;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Process implements Observer {
    private Host me;
    private HashMap<Integer, Host> hostMap;
    private PerfectLink pl;
    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();
    private DatagramSocket socket;

    public Process(int id, HashMap<Integer, Host> hostMap) {
        this.me = hostMap.get(id);
        this.hostMap = hostMap;

        try {
            this.socket = new DatagramSocket(this.me.getPort());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.pl = new PerfectLink(this.me.getPort(), this, this.socket);
    }

    public void send(Message message) {
        Host host = hostMap.get(message.getReceiverId());
        if (host == null) {
            System.out.println("Host " + message.getReceiverId() + " not found");
            return;
        }
        pl.send(message, host);
        logs.add("b " + message.getMessageId() + '\n');
    }

    public int getId() {
        return me.getId();
    }

    public void stopProcessing() {
        PerfectLink.stop();
        // this.socket.close();
    }

    public void startProcessing() {
        pl.start();
    }

    public ConcurrentLinkedQueue<String> getLogs() {
        return logs;
    }

    @Override
    public void deliver(Message message) {
        logs.add("d " + message.getSenderId() + " " + message.getMessageId() + '\n');
    }

}
