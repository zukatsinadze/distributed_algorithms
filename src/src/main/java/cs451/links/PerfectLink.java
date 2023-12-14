package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;
import cs451.lattice.LatticeAgreement;

import java.util.*;

public class PerfectLink implements Observer {
    private final StubbornLink stubbornLinks;
    private final LatticeAgreement deliverer;

    public PerfectLink(int port,
                        byte myId,
                        LatticeAgreement deliverer,
                        HashMap<Byte, Host> hosts,
                        int proposalSetSize) {
        this.stubbornLinks = new StubbornLink(this, myId, port, hosts, proposalSetSize);
        this.deliverer = deliverer;
    }

    public void send(Message message) {
        stubbornLinks.send(message);
    }

    public void stop() {
        stubbornLinks.stop();
    }


    public void start() {
        stubbornLinks.start();
    }

    @Override
    public void deliver(Message message) {
        deliverer.deliver(message);
    }
}
