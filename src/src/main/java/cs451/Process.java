package cs451;

import cs451.links.PerfectLink;

import java.io.FileOutputStream;
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
    private final String output;

    private final Object logLock = new Object(); // Lock for synchronized access to the logs
    private final Object outputLock = new Object();
    private HashMap<Byte, HashSet<Integer>> logsCopy;

    public Process(byte id, HashMap<Byte, Host> hostMap, String output) {
        this.id = id;
        this.me = hostMap.get(id);
        this.pl = new PerfectLink(this.me.getPort(), this, hostMap);
        this.output = output;

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

                    if (curr_size > 10000) {
                        synchronized (logLock) {
                            logsCopy = new HashMap<>(logs);
                            for (Byte key : logs.keySet()) {
                                logs.put(key, new HashSet<>());
                            }
                        }

                        // System.out.println("Dumping logs: " + logsCopy.size());

                        synchronized (outputLock) {
                            FileOutputStream outputStream = new FileOutputStream(output, true);
                            for (Byte key : logsCopy.keySet()) {
                                if (key == id) {
                                    for (Integer msgId : logsCopy.get(key)) {
                                        outputStream.write(("b " + msgId + '\n').getBytes());
                                    }
                                }
                                else {
                                    for (Integer msgId : logsCopy.get(key)) {
                                        outputStream.write(("d " + key + " " + msgId + '\n').getBytes());
                                    }
                                }
                            }
                            outputStream.close();
                        }
                        logsCopy.clear();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 100);

    }


    public void send(Message message) {
        pl.send(message);
        synchronized (logLock) {
            logs.get(id).add(message.getMessageId());
        }
    }

    public byte getId() {
        return me.getId();
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

    @Override
    public void deliver(MessageBatch messages) {
    }

    public void dumpLogs() {
        try {
            FileOutputStream outputStream = new FileOutputStream(output, true);
            synchronized (outputLock) {
                if (logsCopy != null) {
                    for (Byte key : logsCopy.keySet()) {
                        if (key == id) {
                            for (Integer msgId : logsCopy.get(key)) {
                                outputStream.write(("b " + msgId + '\n').getBytes());
                            }
                        }
                        else {
                            for (Integer msgId : logsCopy.get(key)) {
                                outputStream.write(("d " + key + " " + msgId + '\n').getBytes());
                            }
                        }
                    }
                }

                for (Byte key : logs.keySet()) {
                    if (key == id) {
                        for (Integer msgId : logs.get(key)) {
                            outputStream.write(("b " + msgId + '\n').getBytes());
                        }
                    }
                    else {
                        for (Integer msgId : logs.get(key)) {
                            outputStream.write(("d " + key + " " + msgId + '\n').getBytes());
                        }
                    }
                }


                System.out.println("Final Dumping finished");
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
