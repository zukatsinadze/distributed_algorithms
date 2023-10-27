package cs451;

import cs451.links.PerfectLink;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class Process implements Observer {
    private Host me;
    private PerfectLink pl;
    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();
    Queue<String> intermediateLogs;
    private final String output;

    private final AtomicBoolean writing;
    Lock lock = new ReentrantLock();



    public Process(byte id, HashMap<Byte, Host> hostMap, String output) {
        this.me = hostMap.get(id);
        this.pl = new PerfectLink(this.me.getPort(), this, hostMap);
        this.output = output;


        this.writing = new AtomicBoolean(false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if(logs.size() > 100000 && !writing.get()){
                        System.out.println("Logs more than 100000, writing to file");
                        writing.compareAndSet(false, true);
                        // Copy logs to a new queue
                        lock.lock();
                        intermediateLogs = new LinkedList<>(logs);
                        System.out.println("Size of intermediate logs: " + intermediateLogs.size());
                        logs.clear();
                        lock.unlock();
                        try (var outputStream = new FileOutputStream(output, true)) {
                            // Dequeue from logs and write to file
                            while(!intermediateLogs.isEmpty()){
                                // System.out.println("Remaining logs: " + intermediateLogs.size());
                                outputStream.write(intermediateLogs.peek().getBytes());
                                intermediateLogs.remove();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finally {
                            System.out.println("Done writing: Logs more than 100000");
                            writing.compareAndSet(true, false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 10000, 20);
    }

    public void send(Message message) {
        pl.send(message);
        logs.add("b " + message.getMessageId() + '\n');
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

    public ConcurrentLinkedQueue<String> getLogs() {
        return logs;
    }

    @Override
    public void deliver(Message message) {
        logs.add("d " + message.getSenderId() + " " + message.getMessageId() + '\n');
    }

    public  void dumpLogs() {
        while (writing.get()) {
            System.out.println("Waiting for writing to finish");
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(this.output, true);
            for (String log : logs) {
                outputStream.write(log.getBytes());
            }

            for (String log : intermediateLogs) {
                outputStream.write(log.getBytes());
            }

            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
