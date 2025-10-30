package processors;

import models.RespCommand;
import processors.connections.*;
import processors.lists.*;
import processors.rdb.ConfigExecutor;
import processors.rdb.KeysExecutor;
import processors.replication.InfoExecutor;
import processors.replication.ReplconfExecutor;
import processors.sets.*;
import processors.streams.TypeExecutor;
import processors.streams.XaddExecutor;
import processors.streams.XrangeExecutor;
import processors.streams.XreadExecutor;
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
      case "XRANGE" -> new XrangeExecutor().execute(cmd);
      case "XREAD" -> new XreadExecutor().execute(cmd);
      case "CONFIG" -> new ConfigExecutor().execute(cmd);
      case "KEYS" -> new KeysExecutor().execute(cmd);
      case "INFO" -> new InfoExecutor().execute(cmd);
      case "REPLCONF" -> new ReplconfExecutor().execute(cmd);
      case "ZADD" -> new ZaddExecutor().execute(cmd);
      case "ZRANK" -> new ZrankExecutor().execute(cmd);
      case "ZRANGE" -> new ZrangeExecutor().execute(cmd);
      case "ZCARD" -> new ZcardExecutor().execute(cmd);
      case "ZSCORE" -> new ZscoreExecutor().execute(cmd);
      case "ZREM" -> new ZremExecutor().execute(cmd);
      default -> RespUtility.buildErrorResponse("Invalid Command: " + cmd);
    };
  }
}
