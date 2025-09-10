package processors.streams;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class TypeExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      DataStoreValue data = DataStore.get(key);
      return RespUtility.buildSimpleResponse(data == null ? "none" : data.getValueType());
    } catch (Exception e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd == null) {
      throw new IllegalArgumentException("Command cannot be null");
    }
    if (cmd.getArgsSize() != 1) {
      throw new IllegalArgumentException("Wrong number of arguments for 'TYPE' command");
    }
  }
}
