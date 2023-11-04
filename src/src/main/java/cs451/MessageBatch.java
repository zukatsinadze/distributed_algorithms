package cs451;

import java.io.Serializable;
import java.util.List;

public class MessageBatch implements Serializable {
    private List<Message> messages;

    public MessageBatch(List<Message> messages) {
        this.messages = messages;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[Message.SIZE * messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            byte[] messageBytes = messages.get(i).getBytes();
            System.arraycopy(messageBytes, 0, bytes, i * Message.SIZE, Message.SIZE);
        }
        return bytes;
    }

    public static MessageBatch fromBytes(byte[] bytes) {
        int numMessages = bytes.length / Message.SIZE;
        Message[] messages = new Message[numMessages];
        for (int i = 0; i < numMessages; i++) {
            byte[] messageBytes = new byte[Message.SIZE];
            System.arraycopy(bytes, i * Message.SIZE, messageBytes, 0, Message.SIZE);
            messages[i] = Message.fromBytes(messageBytes);
        }
        return new MessageBatch(List.of(messages));
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int size() {
        return messages.size();
    }

    public byte getReceiverId() {
        return messages.get(0).getReceiverId();
    }

}
