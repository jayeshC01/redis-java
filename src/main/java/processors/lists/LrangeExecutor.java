package processors.lists;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.Collections;
import java.util.List;

public class LrangeExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      DataStoreValue data = DataStore.get(key);

      if (data == null) {
        return RespUtility.serializeResponse(Collections.emptyList());
      }

      List<String> element = data.getAsList();
      int size = element.size();
      int start = Integer.parseInt(cmd.getArgs().get(1));
      int end = Integer.parseInt(cmd.getArgs().get(2));
      // Normalize negative indexes
      if (start < 0) start = size + start;
      if (end < 0) end = size + end;

      start = Math.max(0, start);
      end = Math.min(end, size - 1);

      if (start > end || start >= size) {
        return RespUtility.serializeResponse(Collections.emptyList());
      }

      List<String> response = element.subList(start, end + 1);
      return RespUtility.serializeResponse(response);
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    } catch (IllegalStateException e) {
      return RespUtility.buildErrorResponse("WRONGTYPE Operation against a key holding the wrong kind of value");
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() != 3) {
      throw new IllegalArgumentException("wrong number of arguments for LRANGE command");
    }
  }
}
