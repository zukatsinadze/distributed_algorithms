package cs451.links.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import cs451.Message;

public class UDPSender implements Runnable {
    private String serverAddress;
    private int serverPort;
    private Message messageToSend;
    private DatagramSocket socket;

    public UDPSender(String serverAddress, int serverPort, Message messageToSend, DatagramSocket socket) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.messageToSend = messageToSend;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            byte[] sendData = messageToSend.getBytes();
            InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverInetAddress, serverPort);

            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}