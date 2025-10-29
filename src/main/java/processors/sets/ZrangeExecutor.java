package processors.sets;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import models.ZSet;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.ArrayList;
import java.util.List;

public class ZrangeExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      int start = Integer.parseInt(cmd.getArgs().get(1));
      int stop = Integer.parseInt(cmd.getArgs().get(2));

      DataStoreValue value = DataStore.get(key);
      if (value == null) {
        return RespUtility.serializeResponse(new ArrayList<String>());
      }
      if (!value.isZSet()) {
        throw new IllegalStateException("WRONG TYPE Operation against a key holding the wrong kind of value");
      }

      ZSet zset = value.getAsZSet();
      List<String> members = zset.range(start, stop);
      return RespUtility.serializeResponse(members);
    } catch (NumberFormatException e) {
      return RespUtility.buildErrorResponse("value is not an integer or out of range");
    } catch (IllegalArgumentException | IllegalStateException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() != 3) {
      throw new IllegalArgumentException("wrong number of arguments for ZRANGE command");
    }
  }
}
