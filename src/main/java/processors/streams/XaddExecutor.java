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
    try {
      validateCmd(cmd);
      String key = cmd.getKey();
      List<String> args = cmd.getArgs().subList(1, cmd.getArgsSize());
      String entryID = args.getFirst();
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
        stream = existingData.getAsStream();
      }

      String generatedID = resolveStreamID(entryID, stream);
      stream.put(generatedID, entryValue);

      return RespUtility.serializeResponse(generatedID);
    } catch(IllegalArgumentException | IllegalStateException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() < 4) {
      throw new IllegalArgumentException("Wrong number of arguments for XADD command");
    }

    List<String> fieldValues = cmd.getArgs().subList(2, cmd.getArgsSize());
    if (fieldValues.size() % 2 != 0) {
      throw new IllegalArgumentException("XADD requires field-value pairs");
    }
  }

  private void validateStreamID(String entryID, ConcurrentNavigableMap<String, Map<String, String>> stream) {
    if (!entryID.matches("\\d+-\\d+")) {
      throw new IllegalArgumentException("invalid stream ID format");
    }

    String[] parts = entryID.split("-");
    long ms = Long.parseLong(parts[0]);
    long seq = Long.parseLong(parts[1]);

    if (ms == 0 && seq == 0) {
      throw new IllegalArgumentException("The ID specified in XADD must be greater than 0-0");
    }

    if (!stream.isEmpty()) {
      String lastId = stream.lastKey();
      if (!isGreater(entryID, lastId)) {
        throw new IllegalArgumentException("The ID specified in XADD is equal or smaller than the target stream top item");
      }
    }
  }

  private boolean isGreater(String newId, String oldId) {
    String[] newParts = newId.split("-");
    String[] oldParts = oldId.split("-");
    long newMs = Long.parseLong(newParts[0]);
    long newSeq = Long.parseLong(newParts[1]);
    long oldMs = Long.parseLong(oldParts[0]);
    long oldSeq = Long.parseLong(oldParts[1]);

    return (newMs > oldMs) || (newMs == oldMs && newSeq > oldSeq);
  }

  private String resolveStreamID(String entryID, ConcurrentNavigableMap<String, Map<String, String>> stream) {
    long ms, seq;
    if (entryID.equals("*")) {
      ms = System.currentTimeMillis();
      seq = 0;
      if (!stream.isEmpty()) {
        String lastId = stream.lastKey();
        String[] parts = lastId.split("-");
        long lastMs = Long.parseLong(parts[0]);
        long lastSeq = Long.parseLong(parts[1]);

        if (lastMs == ms) {
          seq = lastSeq + 1;
        }
      }

      return ms + "-" + seq;
    }

    if (entryID.endsWith("-*")) {
      ms = Long.parseLong(entryID.split("-")[0]);
      seq = ms == 0 ? 1 : 0;

      if (!stream.isEmpty()) {
        String lastId = stream.lastKey();
        String[] parts = lastId.split("-");
        long lastMs = Long.parseLong(parts[0]);
        long lastSeq = Long.parseLong(parts[1]);

        if (lastMs == ms) {
          seq = lastSeq + 1;
        }
      }

      String newId = ms + "-" + seq;
      validateStreamID(newId, stream);
      return newId;
    }

    validateStreamID(entryID, stream);
    return entryID;
  }
}