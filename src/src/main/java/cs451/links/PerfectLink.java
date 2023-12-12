package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;
import cs451.lattice.LatticeAgreement;

import java.util.*;

public class PerfectLink implements Observer {
    private final StubbornLink stubbornLinks;
    private final LatticeAgreement deliverer;
    private final HashSet<Byte> delivered;
    private final HashMap<Byte, Host> hosts;
    private final byte myId;
    private int ackCount;
    private int nackCount;

    public PerfectLink(int port,
                        byte myId,
                        LatticeAgreement deliverer,
                        HashMap<Byte, Host> hosts,
                        int proposalSetSize) {
        this.stubbornLinks = new StubbornLink(this, myId, port, hosts, proposalSetSize);
        this.hosts = hosts;
        this.ackCount = 0;
        this.nackCount = 0;
        this.myId = myId;
        this.deliverer = deliverer;
        delivered = new HashSet<>();
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

    public int getCurrentRound(){
        return deliverer.getLatticeRound();
    }

    @Override
    public void deliver(Message message) {
        if(message.getLatticeRound() == deliverer.getLatticeRound()){ // The message I got is from the same round
            if(message.isAckOrNAck()){
              if(message.getMessageId() == deliverer.getActiveProposalNumber())
                if(delivered.add(message.getSenderId())) {
                    if (message.isAck()) {
                        ackCount++;
                    }
                    else {
                        nackCount++;
                        deliverer.updateCurrentProposal(message.getProposal());
                    }
                    if(ackCount + nackCount > hosts.size()/2){
                        delivered.clear();
                        stubbornLinks.clearPools();
                        if(nackCount == 0){
                            deliverer.decide();
                        }
                        else {
                            deliverer.broadcastNewProposal();
                        }
                        ackCount = 0;
                        nackCount = 0;
                    }
                }
            }
            else{
                // Compare the message's proposal set to the current proposal set
                // If the message's proposal set is a subset of the current proposal set, then send an ACK
                for(int proposal : message.getProposal()){
                    if(!deliverer.getCurrentProposal().contains(proposal)){
                        // Send a NACK
                        deliverer.updateCurrentProposal(message.getProposal());
                        send(message.nack(deliverer.getCurrentProposal()));
                        return;
                    }
                }
                // Send an ACK
                deliverer.updateCurrentProposal(message.getProposal());
                send(message.ack(deliverer.getCurrentProposal()));
            }
        }
        else{
            if(message.isAckOrNAck()) {
                if(message.getLatticeRound() > deliverer.getLatticeRound()){
                    // Because I am behind, I haven't yet sent proposals for this round, thus it should be impossible for me to receive an ACK message
                    System.out.println("SHOULD NEVER HAPPEN" + message);
                    System.out.print("Message id, sender, reciever, ack " + message.getMessageId() + " " + message.getSenderId() + " " + message.getReceiverId() + " " + message.isAck());
                    System.out.print(" My lattice round " + deliverer.getLatticeRound());
                    System.out.println(" Message lattice round " + message.getLatticeRound());
                }
                // Because I am ahead, I have already decided on a proposal for this round, thus I don't care for the ACK or NACK messages I receive after I have decided.
                return;
            }
            // If we get here the message I receive is a proposal and is from a different round
            if(message.getLatticeRound() > deliverer.getLatticeRound()) { // It's from a future round, so we save it for now and wait for the round to come, don't forget to send an ACK message
                deliverer.getProposal(message.getLatticeRound()).addAll(message.getProposal());
                send(message.ack(deliverer.getProposal(message.getLatticeRound())));
            }
            if(message.getLatticeRound() < deliverer.getLatticeRound()){ // It's from a previous round,
                for(int proposal : message.getProposal()){
                    if(!deliverer.getProposal(message.getLatticeRound()).contains(proposal)){
                        // Send a NACK
                        deliverer.getProposal(message.getLatticeRound()).addAll(message.getProposal());
                        send(message.nack(deliverer.getProposal(message.getLatticeRound())));
                        return;
                    }
                }
                // Send an ACK
                deliverer.getProposal(message.getLatticeRound()).addAll(message.getProposal());
                send(message.ack(deliverer.getProposal(message.getLatticeRound())));
            }
        }
    }
}
