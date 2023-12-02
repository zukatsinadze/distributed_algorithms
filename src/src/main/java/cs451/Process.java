package cs451;

import cs451.links.PerfectLink;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;



public class Process implements Observer {
    private final byte id;
    private Host me;
    private PerfectLink pl;

    private final ConcurrentHashMap<Byte, HashSet<Integer>> logs = new ConcurrentHashMap<>();

    private final Object logLock = new Object(); // Lock for synchronized access to the logs
    private final Object outputLock = new Object();
    private HashMap<Byte, HashSet<Integer>> logsCopy;

    static Logger outputWriter;

    public Process(byte id, HashMap<Byte, Host> hostMap, String output) {
        this.id = id;
        this.me = hostMap.get(id);
        this.pl = new PerfectLink(this.me.getPort(), this, hostMap);

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
                                if (key == id) {
                                    for (Integer msgId : logsCopy.get(key)) {
                                        outputWriter.sent(msgId);
                                    }
                                }
                                else {
                                    for (Integer msgId : logsCopy.get(key)) {
                                        outputWriter.delivered(key, msgId);
                                    }
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


    public void send(Message message) {
        pl.send(message);
        synchronized (logLock) {
            logs.get(id).add(message.getMessageId());
        }
    }

    public byte getId() {
        return id;
    }

    public void stopProcessing() {
        pl.stop();
    }

    public void startProcessing() {
        pl.start();
    }

    @Override
    public void deliver(Message message) {
        synchronized (logLock) {
            logs.get(message.getSenderId()).add(message.getMessageId());
        }
    }

    public void dumpLogs() {
        try {
            synchronized (outputLock) {
                if (logsCopy != null) {
                    for (Byte key : logsCopy.keySet()) {
                        if (key == id) {
                            for (Integer msgId : logsCopy.get(key)) {
                              outputWriter.sent(msgId);
                            }
                        }
                        else {
                            for (Integer msgId : logsCopy.get(key)) {
                              outputWriter.delivered(key, msgId);
                            }
                        }
                    }
                }

                for (Byte key : logs.keySet()) {
                    if (key == id) {
                        for (Integer msgId : logs.get(key)) {
                            outputWriter.sent(msgId);
                        }
                    }
                    else {
                        for (Integer msgId : logs.get(key)) {
                            outputWriter.delivered(key, msgId);
                        }
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
