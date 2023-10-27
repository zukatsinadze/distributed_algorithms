package cs451;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    static HashMap<Byte, Host> hostMap = new HashMap<>();
    static int numberOfMessages;
    static byte targetId;
    static Process process;
    static Parser parser;

    private static void handleSignal() {
        // immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        process.stopProcessing();

        // write/flush output file if necessary
        System.out.println("Writing output.");
        dumpLogs();
        checkNumberOfLogsIsCorrect();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    private static void parseConfig() {
        try (BufferedReader br = new BufferedReader(new FileReader(parser.config()))) {
            String[] parts = br.readLine().split(" ");
            numberOfMessages = Integer.parseInt(parts[0]);
            targetId = Byte.parseByte(parts[1]);

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

    private static void sendMessages() {
        if (process.getId() != targetId) {
            for (int i = 1; i < numberOfMessages + 1; i++) {
                process.send(new Message(i, process.getId(), targetId, ""));
            }
        }
    }

    private static void dumpLogs() {
        process.dumpLogs();
        // ConcurrentLinkedQueue<String> logs = process.getLogs();
        // try {
        //     FileOutputStream outputStream = new FileOutputStream(parser.output(), true);
        //     for (String log : logs) {
        //         outputStream.write(log.getBytes());
        //     }
        //     outputStream.close();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    private static void checkNumberOfLogsIsCorrect() {
        if (targetId == parser.myId()) {
            // I am receiver
            if (process.getLogs().size() != numberOfMessages * (parser.hosts().size() - 1)) {
                System.out.println("Number of logs is not correct for the receiver " +
                        parser.myId());
                System.out.println("Expected: " +
                        numberOfMessages * (parser.hosts().size() - 1));
                System.out.println("Actual: " + process.getLogs().size());
                return;
            }
        } else {
            // I am sender
            if (process.getLogs().size() != numberOfMessages) {
                // This seems to be mostly correct during stresstest
                System.out.println("Number of logs is not correct for the sender " +
                        parser.myId());
                System.out.println("Expected: " + numberOfMessages);
                System.out.println("Actual: " + process.getLogs().size());
                return;
            }
        }
        System.out.println("Number of logs is correct for the process " +
                parser.myId());
    }

    public static void main(String[] args) throws InterruptedException {
        parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid +
                "` or `kill -SIGTERM " + pid +
                "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host : parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
            hostMap.put(host.getId(), host);
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        parseConfig();

        process = new Process(parser.myId(), hostMap, parser.output());
        process.startProcessing();

        System.out.println("Broadcasting and delivering messages...\n");
        sendMessages();

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
