package processors.streams;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

public class XrangeExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCommand(cmd);

      String key = cmd.getKey();
      DataStoreValue value = DataStore.get(key);
      if (value == null || value.getAsStream().isEmpty()) {
        return RespUtility.serializeResponse(Collections.emptyList());
      }

      ConcurrentNavigableMap<String, Map<String, String>> existingData = value.getAsStream();
      String start = cmd.getArgs().get(1).equals("-") ? existingData.firstKey() : cmd.getArgs().get(1);
      String end = cmd.getArgs().get(2).equals("+") ? existingData.lastKey() : cmd.getArgs().get(2);
      ConcurrentNavigableMap<String, Map<String, String>> output =
          existingData.subMap(start, true, end, true);
      if (output.isEmpty()) {
        return RespUtility.serializeResponse(Collections.emptyList());
      }

      return RespUtility.serializeStreamResponse(output);
    } catch (IllegalArgumentException | IllegalStateException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCommand(RespCommand cmd) {
    if (cmd.getKey() == null) {
      throw new IllegalArgumentException("Invalid Args - Missing Key");
    }
    if (cmd.getArgsSize() != 3) {
      throw new IllegalArgumentException("Invalid Args: Missing Start or End");
    }

    String start = cmd.getArgs().get(1);
    String end = cmd.getArgs().get(2);
    if (start.equals("-") || end.equals("+")) {
      return;
    }

    String[] startRange = start.split("-");
    String[] endRange = end.split("-");
    if (Long.parseLong(startRange[0]) == Long.parseLong(endRange[0])) {
      if (Long.parseLong(startRange[1]) > Long.parseLong(endRange[1])) {
        throw new IllegalArgumentException("Start greater than end");
      }
    } else if (Long.parseLong(startRange[0]) > Long.parseLong(endRange[0])) {
      throw new IllegalArgumentException("Start greater than end");
    }
  }
}