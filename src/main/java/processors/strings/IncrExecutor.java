package processors.strings;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class IncrExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      DataStoreValue data = DataStore.get(key);

      if (data == null || data.isExpired()) {
        DataStore.put(key, new DataStoreValue("1"));
        return RespUtility.serializeResponse(1);
      }

      long existingValue = data.getAsLong();
      long newValue = existingValue + 1;
      data.updateValue(String.valueOf(newValue));
      DataStore.put(key, data);
      return RespUtility.serializeResponse(newValue);
    } catch (NumberFormatException e) {
      return RespUtility.buildErrorResponse("value is not an integer or out of range");
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() != 1) {
      throw new IllegalArgumentException("wrong number of arguments for INCR command");
    }
  }
}
