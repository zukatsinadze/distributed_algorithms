package cs451;


import java.io.IOException;
import java.util.HashMap;
import java.util.Set;


public class Process implements Observer {
    private final byte id;
    private Host me;
    private int ds;
    private int vs;
    static Logger outputWriter;


    public Process(byte id, HashMap<Byte, Host> hostMap, String output, int ds, int vs) {
        this.id = id;
        this.me = hostMap.get(id);
        this.ds = ds;
        this.vs = vs;

        try {
          outputWriter = new Logger(output);
        } catch (IOException e) {
          e.printStackTrace();
        }
    }


    // public void broadcast(int msgId) {
      // this.fifo.broadcast(msgId, id);
      // outputWriter.sent(msgId);
    // }

    public void send(Set<Integer> set) {
      // this.fifo.send(set, id);
      // outputWriter.sent(set);  
    }

    public byte getId() {
        return id;
    }

    public void stopProcessing() {
        // fifo.stop();
        try {
            outputWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startProcessing() {
        // fifo.start();
    }


    @Override
    public void deliver(Message message) {
        // outputWriter.delivered(message.getOriginalSenderId(), message.getMessageId());
    }

    public int getCurrentRound() {
        return 0;
    }
}