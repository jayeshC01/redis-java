package processors.lists;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.List;

public class RpushExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      DataStoreValue data = DataStore.get(key);

      if (data == null) {
        List<String> values = cmd.getArgs().subList(1, cmd.getArgsSize());
        DataStore.put(key, new DataStoreValue(values));
        DataStore.notifyWaiter(key);
        return RespUtility.serializeResponse(values.size());
      }

      List<String> existingList = data.getAsList();
      existingList.addAll(cmd.getArgs().subList(1, cmd.getArgsSize()));
      DataStore.notifyWaiter(key);
      return RespUtility.serializeResponse(existingList.size());
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    } catch (IllegalStateException e) {
      return RespUtility.buildErrorResponse("WRONGTYPE Operation against a key holding the wrong kind of value");
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() < 2) {
      throw new IllegalArgumentException("wrong number of arguments for RPUSH command");
    }
  }
}
