package cs451;

import cs451.links.PerfectLink;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Process implements Observer {
    private Host me;
    private PerfectLink pl;
    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();

    public Process(byte id, HashMap<Byte, Host> hostMap) {
        this.me = hostMap.get(id);
        this.pl = new PerfectLink(this.me.getPort(), this, hostMap);
    }

    public void send(Message message) {
        pl.send(message);
        logs.add("b " + message.getMessageId() + '\n');
    }

    public byte getId() {
        return me.getId();
    }

    public void stopProcessing() {
        PerfectLink.stop();
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
