package processors.sets;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import models.ZSet;
import processors.CommandExecutor;
import utility.RespUtility;

public class ZaddExecutor implements CommandExecutor {
  @Override
  public String execute(RespCommand cmd) {
    if (cmd.getArgsSize() != 3) {
      return RespUtility.buildErrorResponse("wrong number of arguments for zadd command");
    }

    String key = cmd.getArgs().get(0);
    String scoreStr = cmd.getArgs().get(1);
    String member = cmd.getArgs().get(2);

    double score;
    try {
      score = Double.parseDouble(scoreStr);
      if (Double.isNaN(score)) throw new NumberFormatException("NaN not allowed");
    } catch (NumberFormatException e) {
      return RespUtility.buildErrorResponse("value is not a valid float");
    }

    if (DataStore.containsKey(key)) {
      return RespUtility.serializeResponse(0);
    }

    ZSet z = new ZSet();
    z.insert(score, member);
    DataStore.put(key, new DataStoreValue(z));
    return RespUtility.serializeResponse(1);
  }
}
