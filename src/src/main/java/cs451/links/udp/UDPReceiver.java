package cs451.links.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cs451.Message;
import cs451.MessageBatch;
import cs451.Observer;

public class UDPReceiver implements Runnable {
    private final Observer observer;
    private static boolean isRunning;
    private DatagramSocket socket;
    private ExecutorService deliverer = Executors.newFixedThreadPool(1);
    private int proposalSetSize;

    public UDPReceiver(Observer observer, int port, int proposalSetSize) {
        this.observer = observer;
        this.proposalSetSize = proposalSetSize;
        try {
            this.socket = new DatagramSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            byte[] receiveData = new byte[(11 + 4 * proposalSetSize)];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            isRunning = true;
            while (isRunning) {
                socket.receive(receivePacket);
                // ArrayList<Message> messages = MessageBatch.fromBytes(receivePacket.getData(), proposalSetSize);
                Message message = Message.fromBytes(receivePacket.getData(), proposalSetSize);
                deliverer.submit(() -> {
                  // for (Message message : messages) {
                  //   observer.deliver(message);
                  // }
                  observer.deliver(message);
                });
            }
            deliverer.shutdown();
            this.socket.close();
            System.out.println("UDPReceiver stopped");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopReceiver() {
        isRunning = false;
    }
}
