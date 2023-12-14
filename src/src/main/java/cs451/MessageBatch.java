package cs451;

import java.io.Serializable;
import java.util.ArrayList;

public class MessageBatch implements Serializable {
  private byte senderId;
  private byte receiverId;
  private ArrayList<Message> messages;

  public MessageBatch(byte senderId, byte receiverId) {
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.messages = new ArrayList<>();
  }

  public MessageBatch(ArrayList<Message> messages) {
    this.messages = new ArrayList<>(messages);
    this.senderId = messages.get(0).getSenderId();
    this.receiverId = messages.get(0).getReceiverId();
  }

  public MessageBatch(Message m) {
    this.messages = new ArrayList<>();
    this.messages.add(m);
    this.senderId = m.getSenderId();
    this.receiverId = m.getReceiverId();
  }

  public byte getSenderId() {
    return senderId;
  }

  public byte getReceiverId() {
    return receiverId;
  }

  public byte[] getBytes(int proposalSetSize) {
    int singleMessageSize = 11 + 4 * proposalSetSize;
    byte[] result = new byte[1 + singleMessageSize * messages.size()];
    result[0] = (byte) messages.size();
    for (int i = 0; i < messages.size(); i++) {
      byte[] bytes = messages.get(i).getBytes(proposalSetSize);
      System.arraycopy(bytes, 0, result, 1 + i * singleMessageSize, singleMessageSize);
    }
    return result;
  }

  public static ArrayList<Message> fromBytes(byte[] bytes, int proposalSetSize) {
    ArrayList<Message> result = new ArrayList<>();
    int size = bytes[0];
    int singleMessageSize = 11 + 4 * proposalSetSize;
    for (int i = 0; i < size; i++) {
      byte[] messageBytes = new byte[singleMessageSize];
      System.arraycopy(bytes, 1 + i * singleMessageSize, messageBytes, 0, singleMessageSize);

      Message m = Message.fromBytes(messageBytes, proposalSetSize);
      result.add(m);
    }
    return result;
  }
}
