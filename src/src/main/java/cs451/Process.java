package cs451;

import cs451.fifo.FIFO;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Process implements Observer {
  private final byte id;
  private Host me;
  private FIFO fifo;

  private final ReentrantLock outputLock = new ReentrantLock();
  private int delivered = 0;
  private long startTime;
  static Logger outputWriter;

  public Process(byte id, HashMap<Byte, Host> hostMap, String output) {
    this.id = id;
    this.me = hostMap.get(id);
    this.fifo = new FIFO(id, this.me.getPort(), this, hostMap);
    this.startTime = System.currentTimeMillis();

    try {
      outputWriter = new Logger(output);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void broadcast(int msgId) {
    this.fifo.broadcast(msgId, id);
    outputWriter.sent(msgId);
  }

  public byte getId() { return id; }

  public void stopProcessing() {
    fifo.stop();
    try {
      outputWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void startProcessing() { this.fifo.start(); }

  @Override
  public void deliver(Message message) {
    try {
      outputLock.lock();
      delivered++;
      if (delivered % 100000 == 0) {
        System.out.println("Delivered " + delivered + " messages. Time from start: " + (System.currentTimeMillis() - startTime)*0.001 + " seconds.");
      }
      outputWriter.delivered(message.getOriginalSenderId(),
                             message.getMessageId());
    } finally {
      outputLock.unlock();
    }
  }

}
