package cs451.links.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import cs451.MessageBatch;

public class UDPSender implements Runnable {
    private String serverAddress;
    private int serverPort;
    private MessageBatch messageToSend;
    private DatagramSocket socket;
    private int proposalSetSize;

    public UDPSender(String serverAddress, int serverPort, MessageBatch messageToSend, DatagramSocket socket, int proposalSetSize) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.messageToSend = messageToSend;
        this.socket = socket;
        this.proposalSetSize = proposalSetSize;
    }

    @Override
    public void run() {
        try {
            byte[] sendData = messageToSend.getBytes(proposalSetSize);
            InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverInetAddress, serverPort);
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
