package cs451;

import cs451.links.PerfectLink;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;




public class Process implements Observer {
    private final byte id;
    private Host me;
    private PerfectLink pl;

    private final ConcurrentHashMap<Byte, HashSet<Integer>> logs = new ConcurrentHashMap<>();
    private AtomicInteger curr_size = new AtomicInteger(0);
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
                System.out.println("memory usage in mb: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
                if (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 45 * 1024 * 1024) {
                    System.gc();
                }
            }
        }, 0, 1000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    synchronized (outputLock) {
                        if (curr_size.get() > 10000) {
                            curr_size.set(0);
                            synchronized (logLock) {
                                logsCopy = new HashMap<>(logs);
                                for (Byte key : logs.keySet()) {
                                    logs.put(key, new HashSet<>());



                                    // logs.put(key) = new ConcurrentHashSet<>();
                                }
                            }

                            System.out.println("Dumping logs: " + logsCopy.size());
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
                            logsCopy.clear();
                            outputStream.close();
                            System.out.println("Dumping finished");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 10);

    }


    public void send(Message message) {
        pl.send(message);
        synchronized (logLock) {
            logs.get(id).add(message.getMessageId());
            curr_size.getAndIncrement();
        }
    }

    public byte getId() {
        return me.getId();
    }

    public void stopProcessing() {
        PerfectLink.stop();
    }

    public void startProcessing() {
        pl.start();
    }

    @Override
    public void deliver(Message message) {
        synchronized (logLock) {
            logs.get(message.getSenderId()).add(message.getMessageId());
            curr_size.getAndIncrement();
        }
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
