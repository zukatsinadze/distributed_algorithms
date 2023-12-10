package cs451;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class Logger {
  protected final BufferedWriter writer;

  public Logger(String outputPath) throws IOException {
    this.writer = new BufferedWriter(new FileWriter(outputPath));
  }

  public void decided(Collection<Integer> values, int currentLatticeRound) {
    try {
      if (!values.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (Integer value : values) {
          sb.append(value).append(" ");
        }
        writer.write(sb.toString().trim() + '\n');
      }
    } catch (IOException e) {
      System.out.println("Error writing to file");
    }
  }

  public void flush() throws IOException {
    writer.flush();
    writer.close();
  }
}
