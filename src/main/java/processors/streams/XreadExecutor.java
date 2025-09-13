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
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

public class XreadExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCommand(cmd);

      List<String> args = cmd.getArgs();
      int startOfStreams = indexOfIgnoreCase(args, "STREAMS");
      int startOfBlock = indexOfIgnoreCase(args, "BLOCK");
      int startOfCount = indexOfIgnoreCase(args, "COUNT");

      long blockMs = startOfBlock != -1 ? Long.parseLong(args.get(startOfBlock+1)) : 0;
      long count = startOfCount != -1 ? Long.parseLong(args.get(startOfCount+1)) : -1;

      List<String> streamsAndIDs = args.subList(startOfStreams + 1, args.size());
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

          if ("$".equals(streamId)) {
            if (streamData.isEmpty()) {
              System.out.println("Inside $ check empty stream");
              streamId = "0-0";
            } else {
              System.out.println("Inside $ check last key stream set" + streamData.lastKey());
              streamId = streamData.lastKey();
            }
          }
          ids.set(i, streamId); // Updating the $ in stream key with top most ID

          ConcurrentNavigableMap<String, Map<String, String>> filtered = streamData.tailMap(streamId, false);
          if (count > 0) {
            filtered = filtered.entrySet().stream()
                .limit(count)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (v1, v2) -> v1,
                    ConcurrentSkipListMap::new
                ));
          }
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

        // Adding delay to avoid continuous look up
        long remaining = deadline - System.currentTimeMillis();
        if (remaining > 0) {
          Thread.sleep(Math.min(100, remaining));
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "*-1\r\n";
    } catch (Exception e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCommand(RespCommand cmd) {
    List<String> args = cmd.getArgs();

    int blockIndex = indexOfIgnoreCase(args, "BLOCK");
    if (blockIndex != -1) {
      if (blockIndex + 1 >= args.size()) {
        throw new IllegalArgumentException("Invalid BLOCK option : time not defined");
      }
      String blockTime = args.get(blockIndex + 1);
      try {
        Long.parseLong(blockTime);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid BLOCK time : must be a number", e);
      }
    }

    int countIndex = indexOfIgnoreCase(args, "COUNT");
    if (countIndex != -1) {
      if (countIndex + 1 >= args.size()) {
        throw new IllegalArgumentException("Invalid COUNT option : time not defined");
      }
      String count = args.get(countIndex + 1);
      try {
        Long.parseLong(count);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid COUNT : must be a number", e);
      }
    }

    int streamsIndex = indexOfIgnoreCase(args, "STREAMS");

    if (blockIndex != -1 && blockIndex >= streamsIndex) {
      throw new IllegalArgumentException("Invalid syntax - BLOCK must appear before STREAMS");
    }

    if (countIndex != -1 && countIndex >= streamsIndex) {
      throw new IllegalArgumentException("Invalid syntax - COUNT must appear before STREAMS");
    }

    if (streamsIndex == -1 || streamsIndex == args.size() - 1) {
      throw new IllegalArgumentException("syntax error - STREAMS must be followed by arguments");
    }
    
    List<String> streamsAndIDs = args.subList(streamsIndex + 1, args.size());
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