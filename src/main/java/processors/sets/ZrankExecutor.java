package processors.sets;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class ZrankExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      String member = cmd.getArgs().get(1);

      DataStoreValue value = DataStore.get(key);
      if (value == null) {
        return RespUtility.serializeResponse(null);
      }

      if (!value.isZSet()) {
        throw new IllegalStateException("WRONGTYPE Operation against a key holding the wrong kind of value");
      }

      Integer rank = value.getAsZSet().rankOf(member);
      if (rank == null) {
        return RespUtility.serializeResponse(null);
      }

      return RespUtility.serializeResponse(rank.longValue());

    } catch (IllegalArgumentException | IllegalStateException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() != 2) {
      throw new IllegalArgumentException("wrong number of arguments for ZRANK command");
    }
  }
}
