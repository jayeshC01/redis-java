package processors.replication;

import models.RespCommand;
import processors.CommandExecutor;

import static config.ConfigProcessor.configs;
import static utility.Constants.*;

public class PsyncExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    return String.format("+FULLRESYNC %s %s\r\n",configs.get(MASTER_REPLID), configs.get(MASTER_REPL_OFFSET));
  }
}
