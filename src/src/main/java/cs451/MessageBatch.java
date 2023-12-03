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

  public MessageBatch(ArrayList<Message> messages)  {
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

  public byte getSenderId() { return senderId; }

  public byte getReceiverId() { return receiverId; }

  public byte[] getBytes() {
    byte[] result = new byte[1 + 8 * messages.size()];
    result[0] = (byte) messages.size();
    for (int i = 0; i < messages.size(); i++) {
      byte[] bytes = messages.get(i).getBytes();
      System.arraycopy(bytes, 0, result, 1 + i * 8, 8);
    }
    return result;
  }

  public static ArrayList<Message> fromBytes(byte[] bytes) {
    ArrayList<Message> result = new ArrayList<>();
    int size = bytes[0];
    for (int i = 0; i < size; i++) {
      byte[] messageBytes = new byte[8];
      System.arraycopy(bytes, 1 + i * 8, messageBytes, 0, 8);
      result.add(Message.fromBytes(messageBytes));
    }
    return result;
  }
}
