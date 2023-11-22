package cs451;

import cs451.broadcast.UniformReliableBroadcast;
import cs451.fifo.FIFO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;



public class Process implements Observer {
    private final byte id;
    private Host me;
    private FIFO fifo;

    private final ConcurrentHashMap<Byte, ArrayList<Integer>> logs = new ConcurrentHashMap<>();

    private final Object logLock = new Object(); // Lock for synchronized access to the logs
    private final Object outputLock = new Object();
    private HashMap<Byte, HashSet<Integer>> logsCopy;

    static Logger outputWriter;


    public Process(byte id, HashMap<Byte, Host> hostMap, String output) {
        this.id = id;
        this.me = hostMap.get(id);
        // this.urb = new UniformReliableBroadcast(id, this.me.getPort(), this, hostMap);
        this.fifo = new FIFO(id, this.me.getPort(), this, hostMap);

        try {
            outputWriter = new Logger(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Byte key : hostMap.keySet()) {
            logs.put(key, new ArrayList<>());
        }

        // new Timer().schedule(new TimerTask() {
        //     @Override
        //     public void run() {
        //         try {
        //             int curr_size = 0;
        //             for (byte key : logs.keySet()) {
        //                 curr_size += logs.get(key).size();
        //             }

        //             if (curr_size > 100000) {
        //                 synchronized (logLock) {
        //                     logsCopy = new HashMap<>(logs);
        //                     for (Byte key : logs.keySet()) {
        //                         logs.put(key, new HashSet<>());
        //                     }
        //                 }

        //                 synchronized (outputLock) {
        //                     for (Byte key : logsCopy.keySet()) {
        //                         if (key == id) {
        //                             for (Integer msgId : logsCopy.get(key)) {
        //                                 outputWriter.sent(msgId);
        //                             }
        //                         }
        //                         else {
        //                             for (Integer msgId : logsCopy.get(key)) {
        //                                 outputWriter.delivered(key, msgId);
        //                             }
        //                         }
        //                     }
        //                 }
        //                 logsCopy.clear();
        //             }
        //         } catch (Exception e) {
        //             e.printStackTrace();
        //         }
        //     }
        // }, 100, 100);

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
        try {
            outputWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void startProcessing() {
        this.fifo.start();
    }

    @Override
    public void deliver(Message message) {
        // synchronized (logLock) {
            // logs.get(message.getOriginalSenderId()).add(message.getMessageId());
        // }
        outputWriter.delivered(message.getOriginalSenderId(), message.getMessageId());
    }

    public void dumpLogs() {
        try {
            synchronized (outputLock) {
                synchronized (logLock) {
                    // if (logsCopy != null) {
                    //     for (Byte key : logsCopy.keySet()) {

                    //         for (Integer msgId : logsCopy.get(key)) {
                    //             outputWriter.delivered(key, msgId);
                    //         }
                    //     }
                    // }

                    for (Byte key : logs.keySet()) {
                        Collections.sort(logs.get(key));
                        for (Integer msgId : logs.get(key)) {
                            outputWriter.delivered(key, msgId);
                        }
                    }

                    outputWriter.flush();
                    System.out.println("Final Dumping finished");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
