import java.lang.*;
import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.*;

class ClientHandler implements Runnable {
  private final Socket clientSocket;

  public ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public void run() {
    try {
      BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(clientSocket.getInputStream()));
      BufferedWriter writer =
          new BufferedWriter(
              new OutputStreamWriter(clientSocket.getOutputStream()));
      while (true) {
        List<String> cmdparts = parseRespCommand(reader);
        String response = processCommand(cmdparts);
        writer.write(response);
        writer.flush();
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
      }
    }
  }

  public List<String> parseRespCommand(BufferedReader reader) throws IOException {
    List<String> commandParts = new ArrayList<>();
    String st = reader.readLine();
    if (st == null || !st.startsWith("*")) {
      throw new IOException("Invalid RESP format: Expected array start '*'");
    }
    int noOfElements = Integer.parseInt(st.substring(1));
    for (int i = 0; i < noOfElements; i++) {
      reader.readLine(); // fetching the bulksize of next content ignoring as we are using BufferedReader
      String content = reader.readLine();
      commandParts.add(content);
    }
    return commandParts;
  }

  public String processCommand(List<String> cmd) {
    System.out.println("Processing the command: " + cmd.toString());
    switch (cmd.get(0).toLowerCase()) {
      case "ping":
        return "+PONG\r\n";
      case "echo": {
        if (cmd.size() != 2) {
          return "-ERR invalid command ECHO";
        }
        return "$" + cmd.get(1).length() + "\r\n" + cmd.get(1) + "\r\n";
      }
      default:
        return "Invalid Command";
    }
  }
}
