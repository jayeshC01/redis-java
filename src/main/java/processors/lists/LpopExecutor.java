package processors.lists;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.ArrayList;
import java.util.List;

public class LpopExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      DataStoreValue data = DataStore.get(key);
      if (data == null) {
        return RespUtility.serializeResponse(null);
      }

      List<String> list = data.getAsList();
      if (list.isEmpty()) {
        return RespUtility.serializeResponse(null);
      }

      if (cmd.getArgsSize() == 1) {
        return RespUtility.serializeResponse(list.removeFirst());
      }

      int count = Integer.parseInt(cmd.getArgs().get(1));
      List<String> responses = new ArrayList<>(count);
      for (int i = 0; i < count && !list.isEmpty(); i++) {
        responses.add(list.removeFirst());
      }
      return RespUtility.serializeResponse(responses);
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    } catch (IllegalStateException e) {
      return RespUtility.buildErrorResponse("WRONGTYPE Operation against a key holding the wrong kind of value");
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() < 1 || cmd.getArgsSize() > 2) {
      throw new IllegalArgumentException("wrong number of arguments for LPOP command");
    }

    if (cmd.getArgsSize() == 2) {
      try {
        int count = Integer.parseInt(cmd.getArgs().get(1));
        if (count < 0) {
          throw new IllegalArgumentException("value is out of range, must be positive");
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("value is not an integer or out of range");
      }
    }
  }
}
