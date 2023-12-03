package cs451;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import cs451.fifo.FIFO;


public class Process implements Observer {
    private final byte id;
    private Host me;
    private FIFO fifo;
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

          System.out.println("Delivered " + delivered + " messages. Time from start: " + (System.currentTimeMillis() - startTime)*0.001 + " seconds.");
          System.out.println("Memory usage in mb: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
    }

    public void startProcessing() {
        fifo.start();
    }


    @Override
    public void deliver(Message message) {
        delivered++;
        outputWriter.delivered(message.getOriginalSenderId(), message.getMessageId());
        if (delivered % 1000 == 0) {
          System.out.println("Delivered " + delivered + " messages");
        }
    }
}