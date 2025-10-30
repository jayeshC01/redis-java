package processors.sets;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class ZscoreExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      String member = cmd.getArgs().get(1);

      DataStoreValue v = DataStore.get(key);
      if (v == null) {
        return RespUtility.serializeResponse(null);
      }
      if (!v.isZSet()) {
        throw new IllegalStateException("WRONG TYPE Operation against a key holding the wrong kind of value");
      }

      String scoreStr = v.getAsZSet().scoreOf(member);
      return RespUtility.serializeResponse(scoreStr);

    } catch (IllegalArgumentException | IllegalStateException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() != 2) {
      throw new IllegalArgumentException("wrong number of arguments for ZSCORE command");
    }
  }
}
