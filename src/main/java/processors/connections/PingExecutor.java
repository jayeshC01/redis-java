package processors.connections;

import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class PingExecutor implements CommandExecutor {
  @Override
  public String execute(RespCommand cmd) {
    if (!cmd.getArgs().isEmpty()) {
      return RespUtility.buildSimpleResponse(cmd.getArgs().getFirst());
    }
    return RespUtility.buildSimpleResponse("PONG");
  }
}
