package processors.lists;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.ArrayList;
import java.util.List;

public class LpushExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      DataStoreValue data = DataStore.get(key);
      List<String> valuesToPush = cmd.getArgs().subList(1, cmd.getArgsSize());

      if (data == null) {
        List<String> newList = new ArrayList<>(valuesToPush.size());
        for (int i = valuesToPush.size() - 1; i >= 0; i--) {
          newList.add(valuesToPush.get(i));
        }
        DataStore.put(key, new DataStoreValue(newList));
        return RespUtility.serializeResponse(newList.size());
      }

      List<String> existingList = data.getAsList();
      List<String> updatedList = new ArrayList<>(valuesToPush.size() + existingList.size());
      for (int i = valuesToPush.size() - 1; i >= 0; i--) {
        updatedList.add(valuesToPush.get(i));
      }
      updatedList.addAll(existingList);
      data.updateValue(updatedList);
      return RespUtility.serializeResponse(updatedList.size());
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    } catch (IllegalStateException e) {
      return RespUtility.buildErrorResponse("WRONGTYPE Operation against a key holding the wrong kind of value");
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() < 2) {
      throw new IllegalArgumentException("wrong number of arguments for LPUSH command");
    }
  }
}
