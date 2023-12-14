package cs451;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import cs451.lattice.LatticeAgreement;

public class Process implements Observer {
  private final byte id;
  private Host me;
  private boolean active;
  private LatticeAgreement lattice;
  static Logger outputWriter;

  public Process(byte id, HashMap<Byte, Host> hostMap, String output, int p, int vs, int ds) {
    this.id = id;
    this.me = hostMap.get(id);
    this.lattice = new LatticeAgreement(id, me.getPort(), hostMap, p, vs, ds, this);
    this.active = true;

    try {
      outputWriter = new Logger(output);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void send(Set<Integer> set) {
    if (active)
      this.lattice.propose(set);
  }

  public byte getId() {
    return id;
  }

  public void stopProcessing() {
    active = false;
    lattice.stop();
    try {
      outputWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void startProcessing() {
    active = true;
    lattice.start();
  }

  @Override
  public void deliver(Message message) {
  }

  public void deliver(Set<Integer> numbers) {
    outputWriter.decided(numbers);
  }


}
