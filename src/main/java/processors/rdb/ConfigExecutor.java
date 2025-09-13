package processors.rdb;

import db.DataStore;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//TODO: Implement Support for option SET, RESETSTAT and REWRITE
public class ConfigExecutor implements CommandExecutor {
  private final Set<String> CONFIG_OPTIONS = new HashSet<>(List.of("GET", "SET", "RESETSTAT", "REWRITE"));

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCommand(cmd);

      List<String> args = cmd.getArgs();
      String configOption = args.getFirst();
      if(configOption.equalsIgnoreCase("GET")) {
        String configProperty = args.get(1);
        String configValue = DataStore.getConfig(configProperty);
        if(configValue != null) {
          return RespUtility.serializeResponse(List.of(configProperty, configValue));
        }
        return RespUtility.serializeResponse(Collections.emptyList());
      }

      return RespUtility.buildErrorResponse("Not Implemented: "+ configOption.toUpperCase());
    } catch (Exception e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCommand(RespCommand cmd) {
    List<String> args = cmd.getArgs();
    String configOption = args.getFirst();
    if(!CONFIG_OPTIONS.contains(configOption.toUpperCase())) {
      throw new IllegalArgumentException("Invalid operation for CONFIG");
    }
    if(configOption.equalsIgnoreCase("GET")) {
      if(!(args.size() == 2)) {
        throw new IllegalArgumentException("Invalid number of arguments to CONFIG");
      }
    }
  }
}
