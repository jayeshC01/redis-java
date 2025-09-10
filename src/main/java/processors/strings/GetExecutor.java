package processors.strings;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class GetExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      DataStoreValue data = DataStore.get(cmd.getKey());
      if (data != null && !data.isExpired()) {
        return RespUtility.serializeResponse(data.getAsString());
      }
      return RespUtility.serializeResponse(null);
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() != 1) {
      throw new IllegalArgumentException("wrong number of arguments for GET command");
    }
  }
}
