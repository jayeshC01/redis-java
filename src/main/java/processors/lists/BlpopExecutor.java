package processors.lists;

import db.DataStore;
import models.DataStoreValue;
import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

import java.util.List;
import java.util.Queue;

public class BlpopExecutor implements CommandExecutor {

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      String key = cmd.getKey();
      double timeoutSeconds;
      long timeoutMillis;
      try {
        timeoutSeconds = Double.parseDouble(cmd.getArgs().get(1));
        timeoutMillis = (long) (timeoutSeconds * 1000);
      } catch (NumberFormatException e) {
        return RespUtility.buildErrorResponse("ERR timeout is not a float or integer");
      }

      DataStoreValue data = DataStore.get(key);
      // If data is present fetch and return it
      if (data != null && !data.getAsList().isEmpty()) {
        return RespUtility.serializeResponse(List.of(key, data.getAsList().removeFirst()));
      }

      // Block the thread
      Thread currentThread = Thread.currentThread();
      DataStore.addWaiter(key, currentThread);
      boolean timedOut = false;
      synchronized (currentThread) {
        try {
          if (timeoutSeconds == 0) {
            currentThread.wait();
          } else {
            currentThread.wait(timeoutMillis);
            timedOut = true;
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return RespUtility.buildErrorResponse("Interrupted while waiting");
        }
      }

      data = DataStore.get(key);
      if (data != null && !data.getAsList().isEmpty()) {
        return RespUtility.serializeResponse(List.of(key, data.getAsList().removeFirst()));
      }

      if (timedOut) {
        Queue<Thread> queue = DataStore.getWaiters(key);
        if (queue != null) {
          queue.remove(currentThread);
          if (queue.isEmpty()) {
            DataStore.cleanUpWaiter(key);
          }
        }
        return "*-1\r\n";
      }

      return RespUtility.serializeResponse(null);
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (cmd.getArgsSize() < 2) {
      throw new IllegalArgumentException("wrong number of arguments for BLPOP command");
    }
  }
}
