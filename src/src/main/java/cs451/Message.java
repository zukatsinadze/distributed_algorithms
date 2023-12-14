package cs451;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Message implements Serializable, Comparable<Message> {
  private final int messageId;
  private byte senderId;
  private byte receiverId;
  private byte ack; // 0 for message, 1 for ack, 2 for nack
  private int latticeRound;
  private Set<Integer> proposal;

  public Message(int messageId, byte senderId, byte receiverId, int latticeRound, Set<Integer> proposal) {
    this.messageId = messageId;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.ack = 0;
    this.latticeRound = latticeRound;
    this.proposal = proposal;
  }

  public Message(int messageId, byte senderId, byte receiverId, byte ack, int latticeRound, Set<Integer> proposal) {
    this.messageId = messageId;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.ack = ack;
    this.latticeRound = latticeRound;
    this.proposal = proposal;
  }

  public int getMessageId() {
    return messageId;
  }

  public byte getSenderId() {
    return senderId;
  }

  public byte getReceiverId() {
    return receiverId;
  }

  public int getLatticeRound() {
    return latticeRound;
  }

  public Set<Integer> getProposal() {
    return proposal;
  }

  public Message ack(Set<Integer> newProposal) {
    return new Message(messageId, receiverId, senderId, (byte)1, latticeRound, newProposal);
  }

  public Message nack(Set<Integer> newProposal) {
    return new Message(messageId, receiverId, senderId, (byte)2, latticeRound, newProposal);
  }

  public boolean isAck() {
    return ack == 1;
  }

  public boolean isNAck() {
    return ack == 2;
  }

  public boolean isAckOrNAck() {
    return ack == 1 || ack == 2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Message))
      return false;
    Message message = (Message) o;
    return messageId == message.messageId && senderId == message.senderId &&
        receiverId == message.receiverId && latticeRound == message.latticeRound;
  }

  public int uniqueId() {
    if (ack == 1 || ack == 2)
      return Objects.hash(messageId, receiverId, senderId, latticeRound);
    return Objects.hash(messageId, senderId, receiverId, latticeRound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageId, senderId, receiverId, ack, latticeRound);
  }

  public byte[] getBytes(int proposalSetSize) {
    byte[] result = new byte[11 + 4 * proposalSetSize];

    result[0] = (byte) (messageId >> 24);
    result[1] = (byte) (messageId >> 16);
    result[2] = (byte) (messageId >> 8);
    result[3] = (byte) messageId;

    result[4] = (byte) (latticeRound >> 24);
    result[5] = (byte) (latticeRound >> 16);
    result[6] = (byte) (latticeRound >> 8);
    result[7] = (byte) latticeRound;

    result[8] = senderId;
    result[9] = receiverId;
    result[10] = ack;

    int idx = 0;
    for (Integer i : proposal) {
      result[11 + idx * 4] = (byte) (i >> 24);
      result[12 + idx * 4] = (byte) (i >> 16);
      result[13 + idx * 4] = (byte) (i >> 8);
      result[14 + idx * 4] = (byte) (int) i;
      idx++;
    }
    return result;
  }

  public static Message fromBytes(byte[] bytes, int proposalSetSize) {
    int messageId = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) |
        ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);

    int latticeRound = ((bytes[4] & 0xFF) << 24) | ((bytes[5] & 0xFF) << 16) |
        ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);
    byte senderId = bytes[8];
    byte recieverId = bytes[9];
    byte ack = bytes[10];

    Set<Integer> proposal = new HashSet<>();
    for (int i = 0; i < proposalSetSize; ++i) {
      int idx = 11 + i * 4;
      int number = ((bytes[idx] & 0xFF) << 24) | ((bytes[idx + 1] & 0xFF) << 16) |
          ((bytes[idx + 2] & 0xFF) << 8) | (bytes[idx + 3] & 0xFF);
      if (number != 0)
        proposal.add(number);
    }

    return new Message(messageId, senderId, recieverId, ack, latticeRound, proposal);
  }

  @Override
  public int compareTo(Message o) {
    return Integer.compare(messageId, o.messageId);
  }
}
