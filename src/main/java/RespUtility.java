import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class RespUtility {
  private RespUtility(){}

  public static List<String> parseRespCommand(BufferedReader reader) throws IOException {
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

  public static String buildErrorResponse(String message) {
    return "-ERR "+message+"\r\n";
  }

  public static String buildSimpleResponse(String message) {
    return "+"+message+"\r\n";
  }

  public static String serializeResponse(Object response) {
    if (response instanceof String) {
      return "$" + ((String) response).length() + "\r\n" + (String) response + "\r\n";
    } else if (response instanceof Integer || response instanceof Long) {
      return ":" + response + "\r\n";
    } else if (response instanceof List) {
      if (((List<?>)response).isEmpty()) return "*0\r\n";
      List<String> formatted = ((List<?>)response).stream()
          .map(String::valueOf)
          .collect(Collectors.toList());
      return "*" + ((List<?>)response).size() + "\r\n"+String.join("",formatted);
    }
    return "$-1\r\n"; // default null bulk string response
  }
}
