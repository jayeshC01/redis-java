package processors.sets;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class ZcardExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      DataStoreValue v = DataStore.get(key);

      if (v == null) {
        return RespUtility.serializeResponse(0);
      }
      if (!v.isZSet()) {
        throw new IllegalStateException("WRONG TYPE Operation against a key holding the wrong kind of value");
      }

      int size = v.getAsZSet().size();
      return RespUtility.serializeResponse(size);

    } catch (IllegalArgumentException | IllegalStateException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() != 1) {
      throw new IllegalArgumentException("wrong number of arguments for ZCARD command");
    }
  }
}
