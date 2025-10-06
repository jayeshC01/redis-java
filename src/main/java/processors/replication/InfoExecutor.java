package processors.replication;

import config.ConfigProcessor;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class InfoExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    if (cmd.getArgs().size() != 1) {
      return "-ERR wrong number of arguments for 'info' command\r\n";
    }
    if (!cmd.getArgs().getFirst().equalsIgnoreCase("replication")) {
      return "-ERR Unsupported INFO section\r\n";
    }
    return RespUtility.serializeResponse("role:" + ConfigProcessor.configs.get("role") + "\r\n");
  }
}
