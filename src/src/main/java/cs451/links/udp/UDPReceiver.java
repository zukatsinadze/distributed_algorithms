package cs451.links.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import cs451.Message;
import cs451.Observer;

public class UDPReceiver implements Runnable {
    private int port;
    private final Observer observer;
    private volatile boolean isRunning;

    public UDPReceiver(int port, Observer observer) {
        this.port = port;
        this.observer = observer;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            byte[] receiveData = new byte[1024];
            isRunning = true;
            System.out.println(InetAddress.getLocalHost());
            while (isRunning) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                Message message = Message.fromBytes(receivePacket.getData());
                observer.deliver(message);
            }

            // Close the socket when done
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopReceiver() {
        isRunning = false;
    }
}
