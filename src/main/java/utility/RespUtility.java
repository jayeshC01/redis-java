package utility;

import models.RespCommand;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.stream.Collectors;

public class RespUtility {
  private RespUtility() {}

  /**
   * The command format is *<number of elements>\r\n$<number of bytes of first element>\r\n<first element>\r\n$<number of bytes of second element>\r\n<second element>\r\n ....
   * E.g: *3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n
   *
   * @param reader client input stream
   * @return RespCommand representation
   * @throws IOException IO exception
   */
  public static RespCommand parseRespCommand(BufferedReader reader) throws IOException {
    List<String> args = new ArrayList<>();
    String header = reader.readLine();
    if (header == null || !header.startsWith("*")) {
      throw new IOException("Invalid command - Invalid RESP format: Expected array start '*'");
    }
    int noOfElements = Integer.parseInt(header.substring(1));
    reader.readLine();
    String commandName = reader.readLine();
    for (int i = 0; i < noOfElements - 1; i++) {
      reader.readLine();
      String content = reader.readLine();
      if (content == null) {
        throw new IOException("Invalid command - Missing argument content");
      }
      args.add(content);
    }

    return new RespCommand(commandName, args);
  }

  public static String buildErrorResponse(String message) {
    return "-ERR " + message + "\r\n";
  }

  public static String buildSimpleResponse(String message) {
    return "+" + message + "\r\n";
  }

  public static String serializeResponse(Object response) {
    if (response instanceof String) {
      return "$" + ((String) response).length() + "\r\n" + (String) response + "\r\n";
    } else if (response instanceof Integer || response instanceof Long) {
      return ":" + response + "\r\n";
    } else if (response instanceof List) {
      if (((List<?>) response).isEmpty()) return "*0\r\n";

      boolean isPreformatted = ((List<?>) response).stream()
          .allMatch(item -> item instanceof String &&
              (((String) item).startsWith("$") || ((String) item).startsWith(":")
                  || ((String) item).startsWith("-ERR") || ((String) item).startsWith("+")));
      if (isPreformatted) {
        List<String> formatted = ((List<?>) response).stream()
            .map(String::valueOf)
            .collect(Collectors.toList());
        return "*" + ((List<?>) response).size() + "\r\n" + String.join("", formatted);
      }
      List<String> serialized = ((List<?>) response).stream()
          .map(item -> {
            String str = String.valueOf(item);
            return "$" + str.length() + "\r\n" + str + "\r\n";
          })
          .collect(Collectors.toList());
      return "*" + serialized.size() + "\r\n" + String.join("", serialized);
    }
    return "$-1\r\n"; // default null bulk string response
  }

  public static String serializeStreamResponse(ConcurrentNavigableMap<String, Map<String, String>> output) {
    StringBuilder response = new StringBuilder();
    response.append("*").append(output.size()).append("\r\n");

    output.forEach((k, v) -> {
      String serialisedKey = serializeResponse(k);
      List<String> values = new ArrayList<>();
      v.forEach((field,value) -> {
        values.add(field);
        values.add(value);
      });
      String serialisedValues = serializeResponse(values);
      String serialisedEntry = "*2\r\n".concat(serialisedKey).concat(serialisedValues);
      response.append(serialisedEntry);
    });
    return response.toString();
  }
}
