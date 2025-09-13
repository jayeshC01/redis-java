package processors.streams;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

public class XreadExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCommand(cmd);

      List<String> args = cmd.getArgs();
      int blockMs = -1;
      int blockIndex = indexOfIgnoreCase(args, "BLOCK");
      if (blockIndex != -1 && blockIndex + 1 < args.size()) {
        try {
          blockMs = Integer.parseInt(args.get(blockIndex + 1));
        } catch (NumberFormatException e) {
          return RespUtility.buildErrorResponse("invalid BLOCK timeout");
        }
      }

      int startOfStreams = indexOfIgnoreCase(args, "STREAMS");
      if (startOfStreams == -1 || startOfStreams == args.size() - 1) {
        return RespUtility.buildErrorResponse("syntax error");
      }

      List<String> streamsAndIDs = args.subList(startOfStreams + 1, args.size());
      if (streamsAndIDs.size() % 2 != 0) {
        return RespUtility.buildErrorResponse("wrong number of arguments for STREAMS");
      }

      List<String> streamKeys = streamsAndIDs.subList(0, streamsAndIDs.size() / 2);
      List<String> ids = streamsAndIDs.subList(streamsAndIDs.size() / 2, streamsAndIDs.size());
      long deadline = (blockMs > 0) ? System.currentTimeMillis() + blockMs : Long.MAX_VALUE;

      while (true) {
        List<String> responses = new ArrayList<>();

        for (int i = 0; i < streamKeys.size(); i++) {
          String streamKey = streamKeys.get(i);
          String streamId = ids.get(i);

          DataStoreValue value = DataStore.get(streamKey);
          if (value == null) {
            continue;
          }

          ConcurrentNavigableMap<String, Map<String, String>> streamData = value.getAsStream();
          ConcurrentNavigableMap<String, Map<String, String>> filtered = streamData.tailMap(streamId, false);

          if (!filtered.isEmpty()) {
            responses.add(encodeResponse(filtered, streamKey));
          }
        }

        if (!responses.isEmpty()) {
          StringBuilder result = new StringBuilder();
          result.append("*").append(responses.size()).append("\r\n");
          for (String r : responses) {
            result.append(r);
          }
          return result.toString();
        }

        if (blockMs == -1 || (blockMs > 0 && System.currentTimeMillis() >= deadline)) {
          return "*-1\r\n";
        }
      }
    } catch (IllegalArgumentException | IllegalStateException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCommand(RespCommand cmd) {
    List<String> args = cmd.getArgs();
    int startOfStreams = indexOfIgnoreCase(args, "STREAMS");
    if (startOfStreams == -1 || startOfStreams == args.size() - 1) {
      throw new IllegalArgumentException("syntax error");
    }

    List<String> streamsAndIDs = args.subList(startOfStreams + 1, args.size());
    if (streamsAndIDs.size() % 2 != 0) {
      throw new IllegalArgumentException("wrong number of arguments for STREAMS");
    }
  }

  private int indexOfIgnoreCase(List<String> args, String target) {
    for (int i = 0; i < args.size(); i++) {
      if (target.equalsIgnoreCase(args.get(i))) {
        return i;
      }
    }
    return -1;
  }

  private String encodeResponse(ConcurrentNavigableMap<String, Map<String, String>> output, String key) {
    return "*2\r\n" +
        RespUtility.serializeResponse(key) +
        RespUtility.serializeStreamResponse(output);
  }
}