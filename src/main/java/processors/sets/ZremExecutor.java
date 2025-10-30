package processors.sets;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class ZremExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      String member = cmd.getArgs().get(1);

      DataStoreValue v = DataStore.get(key);
      if (v == null) {
        return RespUtility.serializeResponse(0);
      }
      if (!v.isZSet()) {
        throw new IllegalStateException("WRONG TYPE Operation against a key holding the wrong kind of value");
      }

      boolean removed = v.getAsZSet().removeMember(member);
      return RespUtility.serializeResponse(removed ? 1 : 0);

    } catch (IllegalArgumentException | IllegalStateException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() != 2) {
      throw new IllegalArgumentException("wrong number of arguments for ZREM command");
    }
  }
}
