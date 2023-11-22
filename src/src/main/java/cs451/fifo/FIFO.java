package cs451.fifo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.broadcast.UniformReliableBroadcast;

public class FIFO implements Observer {
    private final UniformReliableBroadcast urb;
    private final byte myId;
    private final Observer observer;
    private final HashMap<Byte, Host> hostMap;
    private final int[] lastDelivered;
    private final int MAX_HANDLING;
    private final List<PriorityQueue<Message>> inFlight;
    private final Lock inFlightLock = new ReentrantLock();



    public FIFO(byte myId, int port, Observer observer, HashMap<Byte, Host> hostMap) {
        this.hostMap = hostMap;
        this.urb = new UniformReliableBroadcast(myId, port, this, hostMap);
        this.observer = observer;
        this.myId = myId;
        this.MAX_HANDLING = 100;

        List<PriorityQueue<Message>> tmpInFlight = new ArrayList<>();
        for (int i = 0; i < hostMap.size(); i++) {
            tmpInFlight.add(new PriorityQueue<>());
        }
        this.inFlight = Collections.unmodifiableList(tmpInFlight);
        this.lastDelivered = new int[hostMap.size()];
    }

    public void broadcast(int messageId, byte originalSenderId) {
        urb.broadcast(messageId, originalSenderId);
    }

    public void stop() {
        urb.stop();
    }

    public void start() {
        urb.start();
    }

    public void deliver(Message m) {
        inFlightLock.lock();

        try {
            inFlight.get(m.getOriginalSenderId()).add(m);

            while (m.getMessageId() == lastDelivered[m.getOriginalSenderId()] + 1) {
                System.out.println("Memory usage in mb: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000);
                // System.out.println("Delivering " + m.getMessageId() + " from " + m.getOriginalSenderId());
                observer.deliver(m);

                lastDelivered[m.getOriginalSenderId()] = m.getMessageId();

                inFlight.get(m.getOriginalSenderId()).poll();

                if (inFlight.get(m.getOriginalSenderId()).isEmpty()) {
                    break;
                } else {
                    m = inFlight.get(m.getOriginalSenderId()).peek();
                }

            }
        } finally {
            inFlightLock.unlock();
        }
    }

}
