package processors.strings;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.List;

public class SetExecutor implements CommandExecutor {
  private static final String NX = "NX";
  private static final String XX = "XX";
  private static final String EX = "EX";
  private static final String PX = "PX";

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      List<String> args = cmd.getArgs();
      String key = cmd.getKey();
      String value = args.get(1);
      String option = args.size() >= 3 ? args.get(2).toUpperCase() : null;
      String expiryArg = args.size() == 4 ? args.get(3) : null;

      long expiryMillis = 0;

      if (EX.equals(option) || PX.equals(option)) {
        if (expiryArg == null) {
          throw new IllegalArgumentException("missing expiry time for EX/PX option");
        }
        try {
          long expiryTime = Long.parseLong(expiryArg);
          expiryMillis = System.currentTimeMillis() + (EX.equals(option) ? expiryTime * 1000 : expiryTime);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("invalid expiry time - must be a number");
        }
      }

      if (NX.equals(option)) {
        if (DataStore.containsKey(key)) {
          return RespUtility.serializeResponse(null);
        }
      } else if (XX.equals(option)) {
        if (!DataStore.containsKey(key)) {
          return RespUtility.serializeResponse(null);
        }
      }

      DataStore.put(key, new DataStoreValue(value, expiryMillis));
      return RespUtility.buildSimpleResponse("OK");
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    List<String> args = cmd.getArgs();
    int size = args.size();

    if (size < 2 || size > 4) {
      throw new IllegalArgumentException("wrong number of arguments for 'SET' command");
    }

    if (size >= 3) {
      String option = args.get(2).toUpperCase();
      if (!NX.equals(option) && !XX.equals(option) && !EX.equals(option) && !PX.equals(option)) {
        throw new IllegalArgumentException("unknown option: " + args.get(2));
      }

      if ((EX.equals(option) || PX.equals(option)) && size != 4) {
        throw new IllegalArgumentException("missing expiry time for EX/PX option");
      }
    }
  }
}
