package cs451.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs451.Host;
import cs451.Message;

public class AgreementInstance {
    private final int consensusNumber;
    private final LatticeAgreement latticeAgreement;
    private boolean active = false;
    private int acks = 0;
    private int nacks = 0;
    private int currentProposalNumber = 0;
    private Set<Integer> proposedValue;
    private Set<Integer> lastBroadcastedProposal;
    private Set<Integer> acceptedValue;
    private HashMap<Byte, Host> hostMap;
    private final byte myId;
    private int[] lastProposalNumberFromNode;
    private final Lock proposalLock = new ReentrantLock();
    private final Lock handleLock = new ReentrantLock();



    public AgreementInstance(byte myId, int consensusNumber, LatticeAgreement lattice, HashMap<Byte, Host> hostMap) {
        this.myId = myId;
        this.consensusNumber = consensusNumber;
        this.proposedValue = new HashSet<>();
        this.acceptedValue = new HashSet<>();
        this.lastProposalNumberFromNode = new int[hostMap.size()];
        this.latticeAgreement = lattice;
        this.hostMap = hostMap;
    }

    public void propose(Set<Integer> proposal) {
        proposalLock.lock();
        try {
            active = true;
            currentProposalNumber++;
            proposedValue.addAll(proposal);
            proposedValue.addAll(acceptedValue);
            broadcast(proposedValue);
        } finally {
            proposalLock.unlock();
        }
    }

    public void handlePackage(Message message) {
        handleLock.lock();
        try {
            if (message.isAckOrNAck())
              handleAckNack(message);
            else if (message.getLatticeRound() >= lastProposalNumberFromNode[message.getSenderId()])
              handleNewMessage(message);
        } finally {
            handleLock.unlock();
        }
    }

    private void handleNewMessage(Message m) {
        lastProposalNumberFromNode[m.getSenderId()] = m.getLatticeRound();
        if (m.getProposal().containsAll(acceptedValue)) {
            acceptedValue = m.getProposal();
            latticeAgreement.sendPerfectLink(m.ack(m.getProposal()));
        } else {
            acceptedValue.addAll(m.getProposal());
            latticeAgreement.sendPerfectLink(m.nack(acceptedValue));
        }
    }

    private void handleAckNack(Message ackOrNack) {
        if (active && ackOrNack.getLatticeRound() == currentProposalNumber) {
            if (ackOrNack.isAck()) {
                acks++;
            } else {
                nacks++;
                proposedValue.addAll(ackOrNack.getProposal());
            }

            if (acks + nacks > hostMap.size() / 2 && nacks != 0) {
              currentProposalNumber++;
              acks = 0;
              nacks = 0;
              broadcast(proposedValue);
            } else if (acks > hostMap.size() / 2) {
                latticeAgreement.decide(lastBroadcastedProposal, consensusNumber);
                active = false;
            }
        }
    }

    public void rebroadcast() {
        broadcast(proposedValue);
    }

    private void broadcast(Set<Integer> proposal) {
        lastBroadcastedProposal = new HashSet<>(proposal);
        acceptedValue.addAll(proposal);

        if (proposal.containsAll(acceptedValue)) {
            acks++;
        } else {
            nacks++;
            proposedValue.addAll(acceptedValue);
        }

        for (byte host : hostMap.keySet()) {
            if (host != myId) {
                latticeAgreement.sendPerfectLink(new Message(consensusNumber, myId, host, currentProposalNumber, lastBroadcastedProposal));
            }
        }
    }

}
