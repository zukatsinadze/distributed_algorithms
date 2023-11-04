package cs451;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;


public class Logger {
    protected final BufferedWriter writer;

    public Logger(String outputPath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(outputPath));
    }

    public void delivered(int senderId, int messageId) {
        try {
            writer.write("d " + senderId + " " + messageId + '\n');
        } catch (IOException e) {
            System.out.println("Error writing to file");
        }

    }

    public void sent(int messageId) {
        try {
            writer.write("b " + messageId + '\n');
        } catch (IOException e) {
            System.out.println("Error writing to file");
        }
    }

    public void flush() throws IOException{
        writer.flush();
        writer.close();
    }
}
