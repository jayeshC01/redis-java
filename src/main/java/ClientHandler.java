import java.lang.*;
import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

class ClientHandler implements Runnable {
  private final Socket clientSocket;
  private static final Map<String, DataStoreValue> datastore = new ConcurrentHashMap<>();
  private boolean isTransactionEnabled = false;
  private final List<List<String>> queuedCommands = new ArrayList<>();

  public ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public void run() {
    String response;
    try {
      BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(clientSocket.getInputStream()));
      BufferedWriter writer =
          new BufferedWriter(
              new OutputStreamWriter(clientSocket.getOutputStream()));
      while(true) {
        List<String> cmdparts = RespUtility.parseRespCommand(reader);
        if(isTransactionEnabled && !cmdparts.get(0).equalsIgnoreCase("DISCARD")) {
          response = queueCommands(cmdparts);
        } else {
          response = processCommand(cmdparts);
        }
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
    return switch (cmd.get(0).toUpperCase()) {
      case "PING" -> RespUtility.buildSimpleResponse("PONG");
      case "ECHO" -> processCommandEcho(cmd);
      case "SET" -> processCommandSet(cmd);
      case "GET" -> processCommandGet(cmd);
      case "INCR" -> processCommandIncr(cmd);
      case "MULTI" -> processCommandMulti();
      case "EXEC" -> processCommandExec();
      case "DISCARD" -> processCommandDiscard();
      default -> RespUtility.buildErrorResponse("Invalid Command: " + cmd);
    };
  }

  private String processCommandEcho(List<String> cmd) {
    if (cmd.size() != 2) {
      return RespUtility.buildErrorResponse("invalid command ECHO - wrong number of arguments");
    }
    //return "$" + cmd.get(1).length() + "\r\n" + cmd.get(1) + "\r\n";
    return RespUtility.serializeResponse(cmd.get(1));
  }

  private String processCommandGet(List<String> cmd) {
    if (cmd.size() != 2) {
      return RespUtility.buildErrorResponse("invalid command get - wrong number of arguments");
    }
    DataStoreValue data = datastore.get(cmd.get(1));
    if(data!=null && !data.isExpired()) {
      return RespUtility.serializeResponse(data.getValue());
    }
    return RespUtility.serializeResponse(null);
  }

  private String processCommandSet(List<String> cmd) {
    if (cmd.size() < 3 || cmd.size() > 5) {
      return RespUtility.buildErrorResponse("wrong number of arguments for 'SET' command");
    }

    String key = cmd.get(1);
    String value = cmd.get(2);
    String option = cmd.size() >= 4 ? cmd.get(3).toUpperCase() : null;
    String expiryArg = cmd.size() == 5 ? cmd.get(4) : null;
    long expiryMillis = 0;

    if ("EX".equals(option) || "PX".equals(option)) {
      if (expiryArg == null) {
        return RespUtility.buildErrorResponse("missing expiry time for EX/PX option");
      }

      try {
        long expiryTime = Long.parseLong(expiryArg);
        expiryMillis = System.currentTimeMillis() + (
            "EX".equals(option) ? expiryTime * 1000 : expiryTime
        );
      } catch (NumberFormatException e) {
        return RespUtility.buildErrorResponse("invalid expiry time - must be a number");
      }
    }

    if ("NX".equals(option)) {
      if (datastore.containsKey(key)) {
        return RespUtility.serializeResponse(null); // Don't overwrite existing key
      }
    } else if ("XX".equals(option)) {
      if (!datastore.containsKey(key)) {
        return RespUtility.serializeResponse(null); // Don't set if key doesn't exist
      }
    } else if (option != null && !"EX".equals(option) && !"PX".equals(option)) {
      return RespUtility.buildErrorResponse("unknown option:"+ option);
    }

    datastore.put(key, new DataStoreValue(value, expiryMillis));
    return RespUtility.buildSimpleResponse("OK");
  }

  private String processCommandIncr(List<String> cmd) {
    if(cmd.size() != 2){
      return RespUtility.buildErrorResponse("Incorrect argument INCR method");
    }
    DataStoreValue data = datastore.get(cmd.get(1));
    if(data == null || data.isExpired()) {
      datastore.put(cmd.get(1), new DataStoreValue(1));
      return RespUtility.serializeResponse(1);
    }
    try {
      long existingValue = Long.parseLong(data.getValue());
      data.updateValue(String.valueOf(existingValue + 1));
      datastore.put(cmd.get(1), data);
      return RespUtility.serializeResponse(Long.parseLong(data.getValue()));
    } catch(NumberFormatException e) {
      return RespUtility.buildErrorResponse("value is not an integer or out of range");
    }
  }

  private String processCommandMulti() {
    isTransactionEnabled = true;
    return RespUtility.buildSimpleResponse("OK");
  }

  private String processCommandExec() {
    if(!isTransactionEnabled) {
      return RespUtility.buildErrorResponse("EXEC without MULTI");
    }
    isTransactionEnabled = false;
    if(queuedCommands.isEmpty()) {
      return RespUtility.serializeResponse(List.of());
    }
    List<String> responses = queuedCommands.stream()
        .map(this::processCommand)
        .collect(Collectors.toList());
    queuedCommands.clear();
    return RespUtility.serializeResponse(responses);
  }

  private String queueCommands(List<String> cmd) {
    if(!cmd.get(0).equalsIgnoreCase("exec")) {
      queuedCommands.add(cmd);
      return RespUtility.buildSimpleResponse("QUEUED");
    }
    return processCommandExec();
  }

  private String processCommandDiscard() {
    if(isTransactionEnabled){
      queuedCommands.clear();
      isTransactionEnabled=false;
      return RespUtility.buildSimpleResponse("OK");
    }
    return RespUtility.buildErrorResponse("DISCARD without MULTI");
  }
}
