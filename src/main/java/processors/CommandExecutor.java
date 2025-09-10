package processors;

import models.RespCommand;

public interface CommandExecutor {
  /**
   * Executes the Redis command.
   *
   * @param cmd Parsed RESP command object containing command name and arguments.
   * @return RESP-formatted response string.
   */
  String execute(RespCommand cmd);
}
