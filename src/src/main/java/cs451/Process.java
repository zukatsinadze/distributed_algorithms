package cs451;


import java.io.IOException;
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

    private final ConcurrentHashMap<Byte, HashSet<Integer>> logs = new ConcurrentHashMap<>();

    private final Object logLock = new Object(); // Lock for synchronized access to the logs
    private final Object outputLock = new Object();
    private HashMap<Byte, HashSet<Integer>> logsCopy;
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


        for (Byte key : hostMap.keySet()) {
            logs.put(key, new HashSet<>());
        }


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    int curr_size = 0;
                    for (byte key : logs.keySet()) {
                        curr_size += logs.get(key).size();
                    }

                    if (curr_size > 100000) {
                        synchronized (logLock) {
                            logsCopy = new HashMap<>(logs);
                            for (Byte key : logs.keySet()) {
                                logs.put(key, new HashSet<>());
                            }
                        }

                        synchronized (outputLock) {
                            for (Byte key : logsCopy.keySet()) {

                                    for (Integer msgId : logsCopy.get(key)) {
                                        outputWriter.delivered(key, msgId);
                                    }
                            }
                        }
                        logsCopy.clear();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 500);

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
        dumpLogs();
    }

    public void startProcessing() {
        fifo.start();
    }


    @Override
    public void deliver(Message message) {
      synchronized (logLock) {
        delivered++;
        if (delivered % 50 == 0) {
          System.out.println("Delivered " + delivered + " messages. Time from start: " + (System.currentTimeMillis() - startTime)*0.001 + " seconds.");
        }
        logs.get(message.getOriginalSenderId()).add(message.getMessageId());
      }
    }

    public void dumpLogs() {
        try {
            synchronized (outputLock) {
                if (logsCopy != null) {
                    for (Byte key : logsCopy.keySet()) {
                            for (Integer msgId : logsCopy.get(key)) {
                              outputWriter.delivered(key, msgId);
                            }
                    }
                }

                for (Byte key : logs.keySet()) {
                        for (Integer msgId : logs.get(key)) {
                            outputWriter.delivered(key, msgId);
                        }
                }


                System.out.println("Final Dumping finished");
            }
        outputWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}