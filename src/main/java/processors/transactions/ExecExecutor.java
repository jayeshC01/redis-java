package processors.transactions;

import models.RespCommand;
import processors.CommandExecutor;
import processors.CommandProcessor;
import utility.RespUtility;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExecExecutor implements CommandExecutor {
  private final TransactionContext txContext;
  private final CommandProcessor processor;

  public ExecExecutor(TransactionContext txContext, CommandProcessor processor) {
    this.txContext = txContext;
    this.processor = processor;
  }

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      if (!txContext.isTransactionEnabled()) {
        return RespUtility.buildErrorResponse("EXEC without MULTI");
      }

      List<RespCommand> queued = txContext.getQueuedCommands();
      txContext.clearQueuedCommands();
      txContext.disableTransaction();
      if (queued.isEmpty()) {
        return RespUtility.serializeResponse(Collections.emptyList());
      }
      List<String> responses = queued.stream()
          .map(processor::processCommand)
          .collect(Collectors.toList());

      return RespUtility.serializeResponse(responses);
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (!cmd.areArgsEmpty()) {
      throw new IllegalArgumentException("wrong number of arguments for EXEC command");
    }
  }
}
