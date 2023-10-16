package cs451;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private final int messageId;
    private byte senderId;
    private byte receiverId;
    private boolean ack = false;

    public Message(int messageId, byte senderId, byte receiverId) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public Message(int messageId, byte senderId, byte receiverId, boolean ack) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.ack = ack;
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

    public void ack() {
        byte temp = senderId;
        senderId = receiverId;
        receiverId = temp;
        this.ack = true;
    }

    public boolean isAck() {
        return ack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Message))
            return false;
        Message message = (Message) o;
        return messageId == message.messageId && senderId == message.senderId && receiverId == message.receiverId;
    }

    public int uniqueId() {
        if (ack)
            return Objects.hash(messageId, receiverId, senderId);
        return Objects.hash(messageId, senderId, receiverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, senderId, receiverId, ack);
    }

    public byte[] getBytes() {
        return (messageId + " " + senderId + " " + receiverId + " " + ack + " " + "d").getBytes();
    }

    public static Message fromBytes(byte[] bytes) {
        String[] splits = new String(bytes).split(" ");
        return new Message(Integer.parseInt(splits[0]), Byte.parseByte(splits[1]), Byte.parseByte(splits[2]), Boolean.parseBoolean(splits[3]));
    }

}
