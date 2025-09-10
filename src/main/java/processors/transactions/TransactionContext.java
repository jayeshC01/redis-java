package processors.transactions;

import models.RespCommand;

import java.util.ArrayList;
import java.util.List;

public class TransactionContext {
  private boolean transactionEnabled = false;
  private final List<RespCommand> queuedCommands = new ArrayList<>();

  public boolean isTransactionEnabled() {
    return transactionEnabled;
  }

  public void enableTransaction() {
    this.transactionEnabled = true;
  }

  public void disableTransaction() {
    this.transactionEnabled = false;
    queuedCommands.clear();
  }

  public void queueCommand(RespCommand cmd) {
    queuedCommands.add(cmd);
  }

  public List<RespCommand> getQueuedCommands() {
    return new ArrayList<>(queuedCommands);
  }

  public void clearQueuedCommands() {
    queuedCommands.clear();
  }
}
