import java.lang.*;
import java.net.Socket;
import java.io.*;
import java.util.*;

class ClientHandler implements Runnable {

  private final Socket clientSocket;
  private Map<String, DataStoreValue> datastore = new HashMap<>();
  private Set<String> setMethodOptions = Set.of("PX", "EX", "NX", "XX");

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

  private List<String> parseRespCommand(BufferedReader reader) throws IOException {
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

  private String processCommand(List<String> cmd) {
    System.out.println("Processing the command: " + cmd.toString());
    switch (cmd.get(0).toLowerCase()) {
      case "ping":
        return "+PONG\r\n";
      case "echo": {
        if (cmd.size() != 2) {
          return "-ERR invalid command ECHO - wrong number of arguments";
        }
        return "$" + cmd.get(1).length() + "\r\n" + cmd.get(1) + "\r\n";
      }
      case "set": {
        if (cmd.size() < 3 || cmd.size() > 5) {
          return "-ERR wrong number of arguments for 'SET' command";
        }

        String key = cmd.get(1);
        String value = cmd.get(2);
        String option = cmd.size() >= 4 ? cmd.get(3).toUpperCase() : null;
        String expiryArg = cmd.size() == 5 ? cmd.get(4) : null;
        long expiryMillis = 0;

        if ("EX".equals(option) || "PX".equals(option)) {
          if (expiryArg == null) {
            return "-ERR missing expiry time for EX/PX option";
          }

          try {
            long expiryTime = Long.parseLong(expiryArg);
            expiryMillis = System.currentTimeMillis() + (
                "EX".equals(option) ? expiryTime * 1000 : expiryTime
            );
          } catch (NumberFormatException e) {
            return "-ERR invalid expiry time - must be a number";
          }
        }

        if ("NX".equals(option)) {
          if (datastore.containsKey(key)) {
            return "$-1\r\n"; // Don't overwrite existing key
          }
        } else if ("XX".equals(option)) {
          if (!datastore.containsKey(key)) {
            return "$-1\r\n"; // Don't set if key doesn't exist
          }
        } else if (option != null && !"EX".equals(option) && !"PX".equals(option)) {
          return "-ERR unknown option: " + option;
        }

        datastore.put(key, new DataStoreValue(value, expiryMillis));
        return "+OK\r\n";
      }
      case "get": {
        if (cmd.size() != 2) {
          return "-ERR invalid command get - wrong number of arguments";
        }
        DataStoreValue data = datastore.get(cmd.get(1));
        if(data!=null && !data.isExpired()) {
          return "$" + data.getValue().length() + "\r\n" + data.getValue() + "\r\n";
        }
        return "$-1\r\n";
      }
      default:
        return "-ERR Invalid Command";
    }
  }
}
