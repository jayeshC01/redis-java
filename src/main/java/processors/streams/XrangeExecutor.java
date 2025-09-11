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
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
    String key = cmd.getKey();
    DataStoreValue value = DataStore.get(key);
    if (value == null || value.getAsStream().isEmpty()) {
      return RespUtility.serializeResponse(Collections.emptyList());
    }
    String start = cmd.getArgs().get(1);
    String end = cmd.getArgs().get(2);
    ConcurrentNavigableMap<String, Map<String, String>> output = value.getAsStream().subMap(start, true, end, true);
    if(output.isEmpty()) {
      return RespUtility.serializeResponse(Collections.emptyList());
    }
    return RespUtility.serializeStreamResponse(output);
  }

  private void validateCommand(RespCommand cmd) {
    if (cmd.getKey() == null) {
      throw new IllegalArgumentException("Invalid Args - Missing Key");
    }
    if(cmd.getArgsSize() != 3) {
      throw new IllegalArgumentException("Invalid Args: Missing Start or End");
    }

    String[] start = cmd.getArgs().get(1).split("-");
    String[] end = cmd.getArgs().get(2).split("-");
    if(Long.parseLong(start[0]) == Long.parseLong(end[0])) {
      if(Long.parseLong(start[1]) > Long.parseLong(end[1])) {
        throw new IllegalArgumentException("Start greater than end");
      }
    } else if (Long.parseLong(start[0]) > Long.parseLong(end[0])) {
      throw new IllegalArgumentException("Start greater than end");
    }
  }
}