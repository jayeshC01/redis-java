package processors;

import models.RespCommand;
import processors.connections.*;
import processors.lists.*;
import processors.streams.TypeExecutor;
import processors.streams.XaddExecutor;
import processors.strings.*;
import processors.transactions.*;
import utility.RespUtility;

public class CommandProcessor {
  private final TransactionContext txContext = new TransactionContext();
  public String processCommand(RespCommand cmd) {
    if (txContext.isTransactionEnabled()
        && !cmd.getName().equalsIgnoreCase("DISCARD")
        && !cmd.getName().equalsIgnoreCase("EXEC")) {
      txContext.queueCommand(cmd);
      return RespUtility.buildSimpleResponse("QUEUED");
    }

    return switch (cmd.getName().toUpperCase()) {
      case "PING" -> new PingExecutor().execute(cmd);
      case "ECHO" -> new EchoExecutor().execute(cmd);
      case "GET" -> new GetExecutor().execute(cmd);
      case "SET" -> new SetExecutor().execute(cmd);
      case "INCR" -> new IncrExecutor().execute(cmd);
      case "MULTI" -> new MultiExecutor(txContext).execute(cmd);
      case "EXEC" -> new ExecExecutor(txContext, this).execute(cmd);
      case "DISCARD" -> new DiscardExecutor(txContext).execute(cmd);
      case "RPUSH" -> new RpushExecutor().execute(cmd);
      case "LPUSH" -> new LpushExecutor().execute(cmd);
      case "LRANGE" -> new LrangeExecutor().execute(cmd);
      case "LLEN" -> new LlenExecutor().execute(cmd);
      case "LPOP" -> new LpopExecutor().execute(cmd);
      case "BLPOP" -> new BlpopExecutor().execute(cmd);
      case "TYPE" -> new TypeExecutor().execute(cmd);
      case "XADD" -> new XaddExecutor().execute(cmd);
      default -> RespUtility.buildErrorResponse("Invalid Command: " + cmd);
    };
  }
}
