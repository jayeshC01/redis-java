package processors.replication;

import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;
import static config.ConfigProcessor.configs;
import static utility.Constants.*;

public class InfoExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    if (cmd.getArgs().size() != 1) {
      return RespUtility.buildErrorResponse("wrong number of arguments for 'info' command");
    }
    //TODO: Implement other sections of INFO command
    if (!cmd.getArgs().getFirst().equalsIgnoreCase(INFO_SECTION_REPLICATION)) {
      return RespUtility.buildErrorResponse("Unsupported INFO section");
    }
    StringBuilder responseBuilder = new StringBuilder();
    responseBuilder.append("# Replication\r\n");
    responseBuilder.append(ROLE).append(":").append(configs.get(ROLE)).append(CRLF);

    if (configs.containsKey(MASTER_REPLID)) {
      responseBuilder.append(MASTER_REPLID).append(":").append(configs.get(MASTER_REPLID)).append(CRLF);
    }

    if (configs.containsKey(MASTER_REPL_OFFSET)) {
      responseBuilder.append(MASTER_REPL_OFFSET).append(":").append(configs.get(MASTER_REPL_OFFSET)).append(CRLF);
    }
    if (configs.get(ROLE).equalsIgnoreCase(ROLE_SLAVE)) {
      responseBuilder.append(MASTER_HOST).append(":").append(configs.get(MASTER_HOST)).append(CRLF);
      responseBuilder.append(MASTER_PORT).append(":").append(configs.get(MASTER_PORT)).append(CRLF);
      responseBuilder.append(MASTER_LINK_STATUS).append(":").append(MASTER_LINK_UP).append(CRLF);
    }

    String response = responseBuilder.toString();
    return RespUtility.serializeResponse(response);
  }
}
