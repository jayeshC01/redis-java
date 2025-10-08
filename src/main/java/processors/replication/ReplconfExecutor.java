package processors.replication;

import models.RespCommand;
import processors.CommandExecutor;

public class ReplconfExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    return "+OK\r\n";
  }
}
