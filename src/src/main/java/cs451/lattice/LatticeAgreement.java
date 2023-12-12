package cs451.lattice;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.PerfectLink;
import cs451.Process;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LatticeAgreement implements Observer {
  private final PerfectLink perfectLink;
  private final Process observer;
  private final HashMap<Byte, Host> hostMap;
  private final byte myId;
  private AtomicInteger activeProposalNumber;
  private AtomicInteger currentLatticeRound;
  private Set<Integer>[] proposals;

  public LatticeAgreement(byte myId, int port, Process observer,
      HashMap<Byte, Host> hostMap, int proposalSetSize, int latticeRoundCount) {
    this.hostMap = hostMap;
    this.perfectLink = new PerfectLink(port, myId, this, hostMap, proposalSetSize);
    this.observer = observer;
    this.myId = myId;
    this.activeProposalNumber = new AtomicInteger(0);
    this.currentLatticeRound = new AtomicInteger(0);
    this.proposals = new Set[latticeRoundCount];
    for (int i = 0; i < latticeRoundCount; i++) {
      this.proposals[i] = ConcurrentHashMap.newKeySet();
    }

  }

  @Override
  public void deliver(Message message) {
    observer.deliver(message);
  }

  public void broadcast(Set<Integer> proposal) {
    var proposalNumber = activeProposalNumber.incrementAndGet();
    var latticeRound = currentLatticeRound.get();
    proposals[latticeRound].addAll(proposal);
    var prop = proposals[latticeRound];
    for (byte id : hostMap.keySet()) {
      if (id != myId) {
        Message message = new Message(proposalNumber, myId, id, latticeRound, prop);
          perfectLink.send(message);
      }
    }
  }

  public void decide() {
    System.out.println("Delivered" + currentLatticeRound);

    activeProposalNumber.set(0);
    var round = currentLatticeRound.getAndIncrement();
    System.out.println("Deciding set " + round + " " + proposals[round].toString());
    System.out.println("Memory usage in mb: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
    observer.deliver(proposals[round], round);
  }

  public int getActiveProposalNumber() {
    return this.activeProposalNumber.get();
  }

  public Set<Integer> getCurrentProposal() {
    return this.proposals[currentLatticeRound.get()];
  }

  public Set<Integer> getProposal(int latticeRound) {
    return this.proposals[latticeRound];
  }

  public void updateCurrentProposal(Set<Integer> proposals) {
    this.proposals[currentLatticeRound.get()].addAll(proposals);
  }

  public void broadcastNewProposal() {
     var proposalNumber = activeProposalNumber.incrementAndGet();
    var latticeRound = currentLatticeRound.get();
    var prop = proposals[latticeRound];
    for (byte id : hostMap.keySet()) {
      if (id != myId) {
        Message message = new Message(proposalNumber, myId, id, latticeRound, prop);
        perfectLink.send(message);
      }
    }
  }

  public int getLatticeRound() {
    return this.currentLatticeRound.get();
  }

  public void start() {
    this.perfectLink.start();
  }

  public void stop() {
    perfectLink.stop();
  }
}
