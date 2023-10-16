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
        byte[] result = new byte[7];

        result[0] = (byte)(messageId >> 24);
        result[1] = (byte)(messageId >> 16);
        result[2] = (byte)(messageId >> 8);
        result[3] = (byte)messageId;

        result[4] = senderId;
        result[5] = receiverId;

        result[6] = (ack) ? (byte)1 : (byte)0;

        return result;
    }

    public static Message fromBytes(byte[] bytes) {
        int intValue = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        byte byteValue1 = bytes[4];
        byte byteValue2 = bytes[5];
        boolean boolValue = bytes[6] != 0;
        return new Message(intValue, byteValue1, byteValue2, boolValue);
    }

}
