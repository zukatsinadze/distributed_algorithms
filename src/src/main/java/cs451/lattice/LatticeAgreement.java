package cs451.lattice;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.PerfectLink;
import cs451.Process;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class LatticeAgreement implements Observer {
    final int MAX_HANDLING;

    // for child consensus instances
    final PerfectLink perfectLink;
    final byte myId;

    private final Lock handlingNowLock = new ReentrantLock();
    private final Condition canHandleMore = handlingNowLock.newCondition();

    private int nextConsensusNumber = 0;
    private final Map<Integer, AgreementInstance> instances = new HashMap<>();
    private final HashMap<Integer, Set<Integer>> decisionsMap = new HashMap<>();
    private int lastDecided = -1;
    private HashMap<Byte, Host> hostMap;
    private Process process;

    public LatticeAgreement(byte myId, int port, HashMap<Byte, Host> hostMap, int p, int vs, int ds, Process process) {
        this.hostMap= hostMap;
        this.perfectLink = new PerfectLink(port, myId, this, hostMap, ds);
        this.myId = myId;
        this.MAX_HANDLING = Math.min(100, Math.max(8, 10000 / (int) (Math.pow(hostMap.size(), 2))));
        this.process = process;
    }

    public void start() {
        perfectLink.start();
    }

    public void stop() {
        perfectLink.stop();
    }

    public void propose(Set<Integer> proposal) {
        handlingNowLock.lock();

        AgreementInstance instance;
        try {
            while (nextConsensusNumber - lastDecided > MAX_HANDLING) {
                try {
                    canHandleMore.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int consensusN = this.nextConsensusNumber++;

            instance = instances.get(consensusN);
            if (instance == null) {
                instance = new AgreementInstance(myId, consensusN, this, hostMap);
                instances.put(consensusN, instance);
            }
        } finally {
            handlingNowLock.unlock();
        }

        instance.propose(proposal);
    }

    synchronized void decide(Set<Integer> ts, int consensusN) {
      System.out.println("Printing decision: " + consensusN);
      handlingNowLock.lock();
      try {
          decisionsMap.put(consensusN, ts);
          while (decisionsMap.containsKey(lastDecided + 1)) {
              var decision = decisionsMap.remove(lastDecided + 1);
              process.deliver(decision);

              lastDecided++;
              canHandleMore.signal();
          }

      } finally {
          handlingNowLock.unlock();
      }
    }


    @Override
    public void deliver(Message receivedMessage) {
        int consensusN = receivedMessage.getMessageId();

        AgreementInstance instance;

        handlingNowLock.lock();

        try {
            instance = instances.get(consensusN);
            if (instance == null) {
                if (consensusN >= this.nextConsensusNumber) {
                    instance = new AgreementInstance(myId, consensusN, this, hostMap);
                    instances.put(consensusN, instance);
                } else {
                    return;
                }
            }
        } finally {
            handlingNowLock.unlock();
        }

        instance.handlePackage(receivedMessage);
    }

}