package processors.rdb;

import db.DataStore;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.List;
import java.util.Set;

public class KeysExecutor implements CommandExecutor {

  public String execute(RespCommand cmd){
    try {
      validateCommand(cmd);

      Set<String> keys = DataStore.store.keySet();
      return RespUtility.serializeResponse(keys.stream().toList());

    } catch(IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCommand(RespCommand cmd){
    List<String> args = cmd.getArgs();
    if(args.size()!=1) {
      throw new IllegalArgumentException("Invalid number of arguments");
    }
    if(!args.getFirst().equals("*")) {
      throw new IllegalArgumentException("Invalid argument");
    }
  }
}
