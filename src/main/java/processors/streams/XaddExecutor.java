package processors.streams;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class XaddExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    String key = cmd.getKey();
    List<String> args = cmd.getArgs().subList(1, cmd.getArgsSize());

    String entryID = args.get(0);
    Map<String, String> entryValue = new HashMap<>();
    for (int i = 1; i < args.size(); i += 2) {
      entryValue.put(args.get(i), args.get(i + 1));
    }

    DataStoreValue existingData = DataStore.get(key);
    ConcurrentNavigableMap<String, Map<String, String>> stream;
    if (existingData == null) {
      stream = new ConcurrentSkipListMap<>();
      DataStore.put(key, new DataStoreValue(stream));
    } else {
      try {
        stream = existingData.getAsStream();
      } catch (Exception e) {
        return RespUtility.buildErrorResponse(e.getMessage());
      }
    }

    if (!entryID.matches("\\d+-\\d+")) {
      return RespUtility.buildErrorResponse("invalid stream ID format");
    }

    if (stream.containsKey(entryID)) {
      return RespUtility.buildErrorResponse("duplicate ID");
    }

    stream.put(entryID, entryValue);
    return RespUtility.serializeResponse(entryID);
  }
}
