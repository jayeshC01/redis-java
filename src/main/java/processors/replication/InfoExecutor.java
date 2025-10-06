package processors.replication;

import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;
import static config.ConfigProcessor.configs;

public class InfoExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    if (cmd.getArgs().size() != 1) {
      return RespUtility.buildErrorResponse("wrong number of arguments for 'info' command");
    }
    //TODO: Implement other sections of INFO command
    if (!cmd.getArgs().getFirst().equalsIgnoreCase("replication")) {
      return RespUtility.buildErrorResponse("Unsupported INFO section");
    }
    StringBuilder responseBuilder = new StringBuilder();
    responseBuilder.append("# Replication\r\n");
    responseBuilder.append("role:").append(configs.get("role")).append("\r\n");

    if (configs.containsKey("master_replid")) {
      responseBuilder.append("master_replid:").append(configs.get("master_replid")).append("\r\n");
    }

    if (configs.containsKey("master_repl_offset")) {
      responseBuilder.append("master_repl_offset:").append(configs.get("master_repl_offset")).append("\r\n");
    }
    if ("slave".equalsIgnoreCase(configs.get("role"))) {
      responseBuilder.append("master_host:").append(configs.get("masterHost")).append("\r\n");
      responseBuilder.append("master_port:").append(configs.get("masterPort")).append("\r\n");
      responseBuilder.append("master_link_status:up\r\n");
    }

    String response = responseBuilder.toString();
    return RespUtility.serializeResponse(response);
  }
}
