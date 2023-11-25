package cs451;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Main {
    static HashMap<Byte, Host> hostMap = new HashMap<>();
    static int numberOfMessages;
    static Process process;
    static Parser parser;

    private static void handleSignal() {
        // immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        process.stopProcessing();

        // write/flush output file if necessary
        System.out.println("Writing output.");
        dumpLogs();
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
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

    private static void sendMessages() {
        for (int i = 1; i < numberOfMessages + 1; ++i) {
            process.broadcast(i);
        }
    }

    private static void dumpLogs() {
        process.dumpLogs();
    }


    public static void main(String[] args) throws InterruptedException {
        parser = new Parser(args);
        parser.parse();

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
            hostMap.put((byte)(host.getId() - 1), host);
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

        process = new Process((byte)(parser.myId() - 1), hostMap, parser.output());
        process.startProcessing();

        initSignalHandlers();

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
