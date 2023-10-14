package cs451;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private final int messageId;
    private final int senderId;
    private final int receiverId;
    private final String messageContent;
    private int ack = 0;

    public Message(int messageId, int senderId, int receiverId, String messageContent) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageContent = messageContent;
    }

    public Message(int messageId, int senderId, int receiverId, String messageContent, int ack) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageContent = messageContent;
        this.ack = ack;
    }

    public Message(Message m) {
        // Use only for acks
        this.messageId = m.messageId;
        this.senderId = m.receiverId;
        this.receiverId = m.senderId;
        this.messageContent = "ack";
        this.ack = 1;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void ack() {
        this.ack = 1;
    }

    public boolean isAck() {
        return ack == 1;
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
        if (ack == 1)
            return Objects.hash(messageId, receiverId, senderId);
        return Objects.hash(messageId, senderId, receiverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, senderId, receiverId, ack);
    }

    public byte[] getBytes() {
        return (messageId + " " + senderId + " " + receiverId + " " + ack + " " + messageContent).getBytes();
    }

    public static Message fromBytes(byte[] bytes) {
        String[] splits = new String(bytes).split(" ");
        return new Message(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), Integer.parseInt(splits[2]),
                splits[4], Integer.parseInt(splits[3]));
    }

}
