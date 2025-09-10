package processors.lists;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class LlenExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      DataStoreValue data = DataStore.get(key);

      if (data == null) {
        return RespUtility.serializeResponse(0);
      }
      return RespUtility.serializeResponse(data.getAsList().size());
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    } catch (IllegalStateException e) {
      return RespUtility.buildErrorResponse("WRONGTYPE Operation against a key holding the wrong kind of value");
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() != 1) {
      throw new IllegalArgumentException("wrong number of arguments for LLEN command");
    }
  }
}
