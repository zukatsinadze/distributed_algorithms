package cs451.lattice;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.PerfectLink;
import cs451.Process;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LatticeAgreement implements Observer {
  private final int MAX_HANDLING;
  private final PerfectLink perfectLink;
  private final byte myId;
  private final Lock conditionLock = new ReentrantLock();
  private final Condition condition = conditionLock.newCondition();
  private int nextConsensusNumber = 0;
  private int currentlyHandling = 0;
  private final ConcurrentHashMap<Integer, AgreementInstance> instances = new ConcurrentHashMap<>();
  private final HashMap<Integer, Set<Integer>> decisionsMap = new HashMap<>();
  private int nextToDecide = 0;
  private HashMap<Byte, Host> hostMap;
  private Process process;

  public LatticeAgreement(byte myId, int port, HashMap<Byte, Host> hostMap, int p, int vs, int ds, Process process) {
    this.hostMap = hostMap;
    this.perfectLink = new PerfectLink(port, myId, this, hostMap, ds);
    this.myId = myId;
    // this.MAX_HANDLING = Math.min(100, Math.max(8, 10000 / (int) (Math.pow(hostMap.size(), 2))));
    this.MAX_HANDLING = 1;
    this.process = process;
  }

  public void start() {
    perfectLink.start();
  }

  public void stop() {
    System.out.println("Memory usage in mb: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
    perfectLink.stop();
  }

  public void sendPerfectLink(Message m) {
    perfectLink.send(m);
  }

  public void propose(Set<Integer> proposal) {
    conditionLock.lock();
    try {
      while (currentlyHandling > MAX_HANDLING) {
        try {
          condition.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      currentlyHandling++;
    } finally {
      conditionLock.unlock();
    }
    AgreementInstance instance = getAgreementInstance(this.nextConsensusNumber++);
    instance.propose(proposal);
  }

  public void decide(Set<Integer> decidedSet, int consensusNumber) {
    conditionLock.lock();
    try {
      decisionsMap.put(consensusNumber, decidedSet);
      boolean decided = false;
      while (decisionsMap.containsKey(nextToDecide)) {
        decided = true;
        Set<Integer> decision = decisionsMap.remove(nextToDecide);
        process.deliver(decision);
        nextToDecide++;
        currentlyHandling--;
      }
      if (decided)
        condition.signal();
    } finally {
      conditionLock.unlock();
    }
  }

  private AgreementInstance getAgreementInstance(int consensus) {
    AgreementInstance instance = instances.get(consensus);
    if (instance == null) {
      instance = new AgreementInstance(myId, consensus, this, hostMap);
      instances.put(consensus, instance);
    }
    return instance;
  }

  @Override
  public void deliver(Message receivedMessage) {
    AgreementInstance instance = getAgreementInstance(receivedMessage.getMessageId());
    instance.handlePackage(receivedMessage);
  }

}
