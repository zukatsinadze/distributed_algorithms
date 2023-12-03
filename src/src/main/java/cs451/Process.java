package cs451;


import java.io.IOException;
import java.util.HashMap;
import cs451.fifo.FIFO;


public class Process implements Observer {
    private final byte id;
    private Host me;
    private FIFO fifo;
    static Logger outputWriter;

    public Process(byte id, HashMap<Byte, Host> hostMap, String output) {
        this.id = id;
        this.me = hostMap.get(id);
        this.fifo = new FIFO(id, this.me.getPort(), this, hostMap);

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

    public byte getId() {
        return id;
    }

    public void stopProcessing() {
        fifo.stop();
        // dumpLogs();

        try {
            outputWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startProcessing() {
        fifo.start();
    }


    @Override
    public void deliver(Message message) {
        outputWriter.delivered(message.getOriginalSenderId(), message.getMessageId());
    }
}