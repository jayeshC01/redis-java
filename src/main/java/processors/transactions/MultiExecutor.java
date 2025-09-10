package processors.transactions;

import models.RespCommand;
import processors.CommandExecutor;
import utility.RespUtility;

public class MultiExecutor implements CommandExecutor {
  private final TransactionContext txContext;

  public MultiExecutor(TransactionContext txContext) {
    this.txContext = txContext;
  }

  @Override
  public String execute(RespCommand cmd) {
    try {
      validateCmd(cmd);
      txContext.enableTransaction();
      return RespUtility.buildSimpleResponse("OK");
    } catch (IllegalArgumentException e) {
      return RespUtility.buildErrorResponse(e.getMessage());
    }
  }

  private void validateCmd(RespCommand cmd) {
    if (!cmd.areArgsEmpty()) {
      throw new IllegalArgumentException("wrong number of arguments for MULTI command");
    }
  }
}
