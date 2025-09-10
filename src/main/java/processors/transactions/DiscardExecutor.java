package processors.transactions;

import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class DiscardExecutor implements CommandExecutor {
  private final TransactionContext txContext;

  public DiscardExecutor(TransactionContext txContext) {
    this.txContext = txContext;
  }

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);

      if (txContext.isTransactionEnabled()) {
        txContext.clearQueuedCommands();
        txContext.disableTransaction();
        return RespUtility.buildSimpleResponse("OK");
      }

      return RespUtility.buildErrorResponse("DISCARD without MULTI");
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (!cmd.areArgsEmpty()) {
      throw new IllegalArgumentException("wrong number of arguments for DISCARD command");
    }
  }
}
