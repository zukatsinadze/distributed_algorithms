package cs451.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import cs451.Host;
import cs451.Message;

public class AgreementInstance {
    private final int consensusNumber;
    private final LatticeAgreement latticeAgreement;

    private boolean active = false;
    private int ackCount = 0;
    private int nackCount = 0;
    int activeProposalNumber = 0;
    Set<Integer> proposedValue;
    Set<Integer> lastBroadcastedProposal;
    Set<Integer> acceptedValue;
    private HashMap<Byte, Host> hostMap;
    byte myId;


    private int[] latestProposalNumber;

    public AgreementInstance(byte myId, int consensusNumber, LatticeAgreement manager, HashMap<Byte, Host> hostMap) {
      this.myId = myId;
        this.consensusNumber = consensusNumber;
        proposedValue = new HashSet<>();
        acceptedValue = new HashSet<>();
        latestProposalNumber = new int[hostMap.size()];
        this.latticeAgreement = manager;
        this.hostMap = hostMap;
    }

    public void propose(Set<Integer> proposal) {
        synchronized (this) {
            proposedValue.addAll(proposal);
            active = true;
            activeProposalNumber++;

            proposedValue.addAll(acceptedValue);

            broadcastProposal(proposedValue);
        }
    }

    public void handlePackage(Message message) {
        synchronized (this) {
            if (message.isAck())
              handleAck(message);
            else if (message.isNAck())
              handleNack(message);
            else
              handleProposal(message);

        }
    }


    private void handleProposal(Message m) {

        if (m.getMessageId() >= latestProposalNumber[m.getSenderId()]) {
            latestProposalNumber[m.getSenderId()] = m.getMessageId();
        } else {
            return;
        }
        if (m.getProposal().containsAll(acceptedValue)) {
            acceptedValue = m.getProposal();
            latticeAgreement.perfectLink.send(m.ack(m.getProposal()));
        } else {
            addAllToAccepted(m.getProposal());
            latticeAgreement.perfectLink.send(m.nack(acceptedValue));
        }
    }

    private void handleAck(Message ack) {
        if (active && ack.getLatticeRound() == activeProposalNumber) {
            ackCount++;
            ackLogic();
        }
    }

    private void handleNack(Message nack) {
        if (active && nack.getLatticeRound() == activeProposalNumber) {
            nackCount++;
            proposedValue.addAll(nack.getProposal());
            ackLogic();
        }
    }

    private void ackLogic() {
        if (nackCount > 0 && ackCount + nackCount > hostMap.size() / 2) {
            activeProposalNumber++;

            ackCount = 0;
            nackCount = 0;

            broadcastProposal(proposedValue);
        } else if (ackCount > hostMap.size() / 2) {
            latticeAgreement.decide(lastBroadcastedProposal, consensusNumber);
            active = false;
        }
    }

    private void broadcastProposal(Set<Integer> proposal) {
        Set<Integer> copyOfProposal = Set.copyOf(proposal);
        lastBroadcastedProposal = copyOfProposal;


        addAllToAccepted(proposal);


        if (proposal.containsAll(acceptedValue)) {
            ackCount++;
        } else {
            nackCount++;
            proposedValue.addAll(acceptedValue);
        }

        for (byte host : hostMap.keySet()) {
            if (host != myId) {
                latticeAgreement.perfectLink.send(new Message(consensusNumber, myId, host, activeProposalNumber, copyOfProposal));
            }
        }
    }

    private void addAllToAccepted(Set<Integer> values) {
        for (Integer value : values) {
            if (!acceptedValue.contains(value)) {
                acceptedValue.add(value);
            }
        }
    }
}