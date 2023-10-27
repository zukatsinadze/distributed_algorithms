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




public class Process implements Observer {
    private Host me;
    private PerfectLink pl;
    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();
    private final String output;
    private FileOutputStream outputStream;

    private final Object logLock = new Object(); // Lock for synchronized access to the logs
    private final Object outputLock = new Object();
    private Queue<String> logsCopy;

    public Process(byte id, HashMap<Byte, Host> hostMap, String output) {
        this.me = hostMap.get(id);
        this.pl = new PerfectLink(this.me.getPort(), this, hostMap);
        this.output = output;

        try {
            this.outputStream = new FileOutputStream(this.output, true);
        } catch (IOException e) {
            e.printStackTrace();
        }


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    synchronized (outputLock) {
                        if (logs.size() > 10000) {
                            synchronized (logLock) {
                                logsCopy = new LinkedList<>(logs);
                                logs.clear();
                            }
                            System.out.println("Dumping logs: " + logsCopy.size());

                            while(!logsCopy.isEmpty()){
                                outputStream.write(logsCopy.peek().getBytes());
                                logsCopy.remove();
                            }
                            System.out.println("Dumping finished");

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 500);

    }




    public void send(Message message) {
        pl.send(message);
        synchronized (logLock) {
            logs.add("b " + message.getMessageId() + '\n');
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

    public ConcurrentLinkedQueue<String> getLogs() {
        return logs;
    }

    @Override
    public void deliver(Message message) {
        synchronized (logLock) {
            logs.add("d " + message.getSenderId() + " " + message.getMessageId() + '\n');
        }
    }

    public  void dumpLogs() {
        try {
            synchronized (outputLock) {
                if (logsCopy != null)
                    for (String log : logsCopy) {
                        outputStream.write(log.getBytes());
                    }


                for (String log : logs) {
                    outputStream.write(log.getBytes());
                }
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
