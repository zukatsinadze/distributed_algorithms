package cs451;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable, Comparable<Message> {
  private final int messageId;
  private byte senderId;
  private byte receiverId;
  private byte originalSenderId;
  private boolean ack = false;
  private boolean ack_ack = false;

  public Message(int messageId, byte senderId, byte receiverId,
                 byte originalSenderId) {
    this.messageId = messageId;
    this.senderId = senderId;
    this.originalSenderId = originalSenderId;
    this.receiverId = receiverId;
  }

  public Message(int messageId, byte senderId, byte receiverId,
                 byte originalSenderId, boolean ack, boolean ack_ack) {
    this.messageId = messageId;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.originalSenderId = originalSenderId;
    this.ack = ack;
    this.ack_ack = ack_ack;
  }

  public int getMessageId() { return messageId; }

  public byte getSenderId() { return senderId; }

  public byte getReceiverId() { return receiverId; }

  public byte getOriginalSenderId() { return originalSenderId; }

  public void ack() {
    byte temp = senderId;
    senderId = receiverId;
    receiverId = temp;
    this.ack = true;
  }

  public boolean isAckAck() { return ack_ack; }

  public void ack_ack() {
    byte temp = senderId;
    senderId = receiverId;
    receiverId = temp;
    this.ack = true;
    this.ack_ack = true;
  }

  public boolean isAck() { return ack; }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Message))
      return false;
    Message message = (Message)o;
    return messageId == message.messageId && senderId == message.senderId &&
        receiverId == message.receiverId;
  }

  public int uniqueId() {
    if (ack_ack)
      return Objects.hash(messageId, senderId, receiverId, originalSenderId);
    if (ack)
      return Objects.hash(messageId, receiverId, senderId, originalSenderId);
    return Objects.hash(messageId, senderId, receiverId, originalSenderId);
  }

  public int uniqueMessageOriginalSenderId() {
    return Objects.hash(messageId, originalSenderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageId, senderId, receiverId, originalSenderId, ack);
  }

  public byte[] getBytes() {
    byte[] result = new byte[9];

    result[0] = (byte)(messageId >> 24);
    result[1] = (byte)(messageId >> 16);
    result[2] = (byte)(messageId >> 8);
    result[3] = (byte)messageId;

    result[4] = senderId;
    result[5] = receiverId;
    result[6] = originalSenderId;

    result[7] = (ack) ? (byte)1 : (byte)0;
    result[8] = (ack_ack) ? (byte)1 : (byte)0;
    return result;
  }

  public static Message fromBytes(byte[] bytes) {
    int intValue = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) |
                   ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    byte byteValue1 = bytes[4];
    byte byteValue2 = bytes[5];
    byte byteValue3 = bytes[6];
    boolean boolValue1 = bytes[7] != 0;
    boolean boolValue2 = bytes[8] != 0;
    return new Message(intValue, byteValue1, byteValue2, byteValue3, boolValue1,
                       boolValue2);
  }

  @Override
  public int compareTo(Message o) {
    return Integer.compare(messageId, o.messageId);
  }
}
