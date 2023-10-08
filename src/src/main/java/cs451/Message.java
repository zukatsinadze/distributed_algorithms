package cs451;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private final int messageId;
    private final int senderId;
    private final int receiverId;
    private final int ogSenderId;
    private final String messageContent;

    public Message(int messageId, int senderId, int receiverId, int ogSenderId, String messageContent) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.ogSenderId = ogSenderId;
        this.messageContent = messageContent;
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

    public int getogSenderId() {
        return ogSenderId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return messageId == message.messageId && senderId == message.senderId && receiverId == message.receiverId; // && ogSenderId == message.ogSenderId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, ogSenderId);
    }

    public byte[] getBytes() {
        return (messageId + " " + senderId + " " + receiverId + " " + ogSenderId + " " + messageContent).getBytes();
    }

    public static Message fromBytes(byte[] bytes) {
        String[] splits = new String(bytes).split(" ");
        return new Message(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), Integer.parseInt(splits[2]), Integer.parseInt(splits[3]), splits[4]);
    }

}
