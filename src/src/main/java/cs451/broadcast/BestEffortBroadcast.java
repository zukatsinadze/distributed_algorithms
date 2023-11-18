package cs451.broadcast;

import java.util.HashMap;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.PerfectLink;

// Implements:
//     BestEffortBroadcast (beb)
// Uses:
//     PerfectLinks (pp2p)
// Events:
//     Request: <bebBroadcast, m>: broadcasts a message m to all processes
//     Indication: <bebDeliver, src, m>: delivers a message m sent by src

// upon event <bebBroadcast, m> do:
//     forall q ∈ Π do:
//         trigger <pp2pSend, q, m>;

// upon event <pp2pDeliver, src, m> do:
//     trigger <bebDeliver, src, m>;


public class BestEffortBroadcast implements Observer {
    private final PerfectLink perfectLink;
    private final Observer observer;
    private final HashMap<Byte, Host> hostMap;


    public BestEffortBroadcast(int port, Observer observer, HashMap<Byte, Host> hostMap) {
        this.hostMap = hostMap;
        this.perfectLink = new PerfectLink(port, this, hostMap);
        this.observer = observer;
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }

    public void broadcast(int messageId, byte srcId) {
        for (byte key : hostMap.keySet()) {
            if (key != srcId) {
                perfectLink.send(new Message(messageId, srcId, key));
            }
        }
    }

    public void start() {
        this.perfectLink.start();
    }

    public static void stop() {
        PerfectLink.stop();
    }
}
