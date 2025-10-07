package processors.connections;

import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class EchoExecutor implements CommandExecutor {
  @Override
  public String execute(RespCommand cmd) {
    if (cmd.getArgs().isEmpty()) {
      return RespUtility.buildErrorResponse("wrong number of arguments for echo command");
    }
    return RespUtility.buildSimpleResponse(cmd.getArgs().getFirst());
  }
}
