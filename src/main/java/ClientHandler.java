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
      while (true) {
        List<String> cmdparts = RespUtility.parseRespCommand(reader);
        System.out.println("Executing Command: "+cmdparts);
        if(isTransactionEnabled && !cmdparts.get(0).equalsIgnoreCase("DISCARD")) {
          response = queueCommands(cmdparts);
        } else {
          response = processCommand(cmdparts);
        }
        System.out.println("Response Send :"+ response);
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
      case "RPUSH" -> processCommandRpush(cmd);
      case "LRANGE"-> processCommandLrange(cmd);
      case "LPUSH" -> processCommandLpush(cmd);
      default -> RespUtility.buildErrorResponse("Invalid Command: " + cmd);
    };
  }

    private String processCommandLpush(List<String> cmd) {
        DataStoreValue data = datastore.get(cmd.get(1));
        if(data == null) {
            datastore.put(cmd.get(1), new DataStoreValue(cmd.subList(2, cmd.size())));
            return RespUtility.serializeResponse(cmd.size()-2);
        }
        LinkedList<String> existingList = new LinkedList(data.getAsList());
        cmd.subList(2, cmd.size()).forEach(e -> existingList.addFirst(e));
        data.updateValue(existingList);
        return RespUtility.serializeResponse(data.getAsList().size());
    }

    private String processCommandLrange(List<String> cmd) {
        DataStoreValue data = datastore.get(cmd.get(1));
        if(data == null) {
            return RespUtility.serializeResponse(Collections.emptyList());
        }
        try {
            List<String> element = data.getAsList();
            int size = element.size();
            int start = Integer.parseInt(cmd.get(2));
            int end = Integer.parseInt(cmd.get(3));
            // Normalize negative indexes
            if (start < 0) start = size + start;
            if (end < 0) end = size + end;

            // Clamp to valid range
            start = Math.max(0, start);
            end = Math.min(end, size - 1);

            // Handle empty range
            if (start > end || start >= size) {
                return RespUtility.serializeResponse(Collections.emptyList());
            }
            List<String> response = element.subList(start, end + 1);
            return RespUtility.serializeResponse(response);
        } catch (Exception e) {
            return RespUtility.buildErrorResponse("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
    }

    private String processCommandRpush(List<String> cmd) {
    DataStoreValue data = datastore.get(cmd.get(1));
    if(data == null) {
      datastore.put(cmd.get(1), new DataStoreValue(cmd.subList(2, cmd.size())));
      return RespUtility.serializeResponse(cmd.size()-2);
    }
    List<String> existingList = data.getAsList();
    existingList.addAll(cmd.subList(2, cmd.size()));
    return RespUtility.serializeResponse(data.getAsList().size());
  }

  private String processCommandEcho(List<String> cmd) {
    if (cmd.size() != 2) {
      return RespUtility.buildErrorResponse("invalid command ECHO - wrong number of arguments");
    }
    return RespUtility.serializeResponse(cmd.get(1));
  }

  private String processCommandGet(List<String> cmd) {
    if (cmd.size() != 2) {
      return RespUtility.buildErrorResponse("invalid command get - wrong number of arguments");
    }
    DataStoreValue data = datastore.get(cmd.get(1));
    if(data!=null && !data.isExpired()) {
      return RespUtility.serializeResponse(data.getAsString());
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
      datastore.put(cmd.get(1), new DataStoreValue(String.valueOf(1)));
      return RespUtility.serializeResponse(1);
    }
    try {
      long existingValue = data.getAsLong();
      data.updateValue(String.valueOf(existingValue + 1));
      datastore.put(cmd.get(1), data);
      return RespUtility.serializeResponse(data.getAsLong());
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
