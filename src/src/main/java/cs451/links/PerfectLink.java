package cs451.links;

import cs451.Observer;
import cs451.lattice.LatticeAgreement;
import cs451.Host;
import cs451.Message;
import java.util.HashMap;
import java.util.HashSet;


public class PerfectLink implements Observer {
    private final StubbornLink stubbornLink;
    private final LatticeAgreement observer;
    private final HashMap<Byte, Host> hostMap;

    private final HashSet<Byte> delivered;
    private int ackCount;
    private int nackCount;

    public PerfectLink(byte myId, int port, LatticeAgreement observer, HashMap<Byte, Host> hostMap, int proposalSetSize) {
        this.stubbornLink = new StubbornLink(this, myId, port, hostMap, proposalSetSize);
        this.observer = observer;
        this.hostMap = hostMap;
        this.delivered = new HashSet<>();
        this.ackCount = 0;
        this.nackCount = 0;
    }

    public void send(Message message) {
      stubbornLink.send(message);
    }

    public void start() {
        stubbornLink.start();
    }

    public void stop() {
        stubbornLink.stop();
    }

    private void processAckOrNack(Message message) {
        if (delivered.add(message.getSenderId())) {
            if (message.isAck()) {
                ackCount++;
            } else {
                nackCount++;
                observer.updateCurrentProposal(message.getProposal());
            }

            if (ackCount + nackCount >= hostMap.size() / 2) {
                handleDecisionOrBroadcast();
            }
        }
    }

    private void handleDecisionOrBroadcast() {
        delivered.clear();
        if (nackCount == 0) {
            observer.decide();
        } else {
            observer.broadcastNewProposal();
        }
        ackCount = 0;
        nackCount = 0;
    }

    private void processNonAck(Message message) {
        for (int proposal : message.getProposal()) {
            if (!observer.getCurrentProposal().contains(proposal)) {
                // send nack
                observer.updateCurrentProposal(message.getProposal());
                message.nack(observer.getCurrentProposal());
                send(message);
                return;
            }
        }

        // send ack
        observer.updateCurrentProposal(message.getProposal());
        message.ack(observer.getCurrentProposal());
        send(message);
    }

    @Override
    public void deliver(Message message) {
        if (message.getLatticeRound() == observer.getLatticeRound()) {
            if (message.isAckOrNAck() && message.getMessageId() == observer.getActiveProposalNumber()) {
                processAckOrNack(message);
            } else {
                processNonAck(message);
            }
        } else {
            handleDifferentLatticeRound(message);
        }
    }

    private void handleDifferentLatticeRound(Message message) {
        if (message.isAckOrNAck()) {
            return;
        }
        if (message.getLatticeRound() > observer.getLatticeRound()) {
            processLargerLatticeRound(message);
        } else if (message.getLatticeRound() < observer.getLatticeRound()) {
            processSmallerLatticeRound(message);
        }
    }

    private void processLargerLatticeRound(Message message) {
        observer.getProposal(message.getLatticeRound()).addAll(message.getProposal());
        message.ack(observer.getProposal(message.getLatticeRound()));
        send(message);
    }

    private void processSmallerLatticeRound(Message message) {
        for (int proposal : message.getProposal()) {
            if (!observer.getProposal(message.getLatticeRound()).contains(proposal)) {
                // send nack
                observer.getProposal(message.getLatticeRound()).addAll(message.getProposal());
                message.nack(observer.getProposal(message.getLatticeRound()));
                send(message);
                return;
            }
        }

        // send ack
        observer.getProposal(message.getLatticeRound()).addAll(message.getProposal());
        message.ack(observer.getProposal(message.getLatticeRound()));
        send(message);
    }
}
