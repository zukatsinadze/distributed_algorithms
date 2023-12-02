package cs451.links.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import cs451.Message;
import cs451.Observer;

public class UDPReceiver implements Runnable {
    private final Observer observer;
    private static boolean isRunning;
    private DatagramSocket socket;

    public UDPReceiver(Observer observer, int port) {
        this.observer = observer;
        try {
            this.socket = new DatagramSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            byte[] receiveData = new byte[8];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            isRunning = true;
            while (isRunning) {
                socket.receive(receivePacket);
                Message message = Message.fromBytes(receivePacket.getData());
                observer.deliver(message);
            }
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