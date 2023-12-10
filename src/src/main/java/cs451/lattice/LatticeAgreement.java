package cs451.lattice;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.PerfectLink;
import cs451.Process;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LatticeAgreement implements Observer {
  private final PerfectLink perfectLink;
  private final Process observer;
  private final HashMap<Byte, Host> hostMap;
  private final byte myId;
  private int activeProposalNumber;
  private int currentLatticeRound;
  private Set<Integer>[] proposals;

  public LatticeAgreement(byte myId, int port, Process observer,
      HashMap<Byte, Host> hostMap, int proposalSetSize, int latticeRoundCount) {
    this.hostMap = hostMap;
    this.perfectLink = new PerfectLink(myId, port, this, hostMap, proposalSetSize);
    this.observer = observer;
    this.myId = myId;
    this.activeProposalNumber = 0;
    this.currentLatticeRound = 0;
    this.proposals = new Set[latticeRoundCount];
    for (int i = 0; i < latticeRoundCount; i++) {
      this.proposals[i] = new HashSet<>();
    }

  }

  @Override
  public void deliver(Message message) {
    observer.deliver(message);
  }

  public void broadcast(Set<Integer> proposal) {
    activeProposalNumber++;
    proposals[this.currentLatticeRound].addAll(proposal);
    for (byte id : hostMap.keySet()) {
      if (id != myId) {
        Message message = new Message(activeProposalNumber, myId, id, currentLatticeRound, proposals[this.currentLatticeRound]);
          perfectLink.send(message);          
      }
    }
  }

  public void decide() {
    System.out.println("Delivered" + currentLatticeRound);
    observer.deliver(proposals[currentLatticeRound], currentLatticeRound);
    activeProposalNumber = 0;
    currentLatticeRound++;
  }

  public int getActiveProposalNumber() {
    return this.activeProposalNumber;
  }

  public Set<Integer> getCurrentProposal() {
    return this.proposals[currentLatticeRound];
  }

  public Set<Integer> getProposal(int latticeRound) {
    return this.proposals[latticeRound];
  }

  public void updateCurrentProposal(Set<Integer> proposals) {
    this.proposals[currentLatticeRound].addAll(proposals);
  }

  public void broadcastNewProposal() {
    this.activeProposalNumber++;
    for (byte id : hostMap.keySet()) {
      if (id != myId) {
        Message message = new Message(activeProposalNumber, myId, id, currentLatticeRound, proposals[currentLatticeRound]);
        perfectLink.send(message);
      }
    }
  }

  public int getLatticeRound() {
    return this.currentLatticeRound;
  }

  public void start() {
    this.perfectLink.start();
  }

  public void stop() {
    perfectLink.stop();
  }
}
