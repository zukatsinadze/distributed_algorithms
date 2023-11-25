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
import java.util.concurrent.locks.ReentrantLock;



public class Process implements Observer {
    private final byte id;
    private Host me;
    private FIFO fifo;

    private final ConcurrentHashMap<Byte, ArrayList<Integer>> logs = new ConcurrentHashMap<>();

    // private final Object logLock = new Object(); // Lock for synchronized access to the logs
    // private final Object outputLock = new Object();
    private final ReentrantLock logLock = new ReentrantLock();
    private final ReentrantLock outputLock = new ReentrantLock();
    private int delivered = 0;
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
        try {
            outputLock.lock();
            delivered++;
            if (delivered % 1000 == 0) {
                System.out.println("Delivered " + delivered + " messages");
            }
            outputWriter.delivered(message.getOriginalSenderId(), message.getMessageId());
        } finally {
            outputLock.unlock();
        }

    }

    public void dumpLogs() {
        try {
            outputLock.lock();
            outputWriter.flush();
            System.out.println("Final Dumping finished");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outputLock.unlock();
        }
    }

}
