package processors.replication;

import models.RespCommand;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static config.ConfigProcessor.configs;
import static utility.Constants.*;

public class PsyncExecutor {
  
  public byte[] execute(RespCommand cmd) {
    String psyncResponse =
        String.format("+FULLRESYNC %s %s\r\n", configs.get(MASTER_REPLID), configs.get(MASTER_REPL_OFFSET));

    byte[] psyncBytes = psyncResponse.getBytes(StandardCharsets.UTF_8);
    byte[] rdbContents = getRDBFile();
    String rdbHeader = "$" + rdbContents.length + "\r\n";
    byte[] rdbHeaderBytes = rdbHeader.getBytes(StandardCharsets.US_ASCII);
    byte[] combined = new byte[psyncBytes.length + rdbHeaderBytes.length + rdbContents.length];
    int pos = 0;
    System.arraycopy(psyncBytes, 0, combined, pos, psyncBytes.length);
    pos += psyncBytes.length;
    System.arraycopy(rdbHeaderBytes, 0, combined, pos, rdbHeaderBytes.length);
    pos += rdbHeaderBytes.length;
    System.arraycopy(rdbContents, 0, combined, pos, rdbContents.length);

    return combined;
  }

  /**
   * Returns a minimal valid empty RDB file (same as Redis sends during initial sync).
   */
  //TODO: Replace with actual RDB reading logic
  private byte[] getRDBFile() {
    return HexFormat.of().parseHex(
        "524544495330303131" +
            "fa0972656469732d76657205372e322e30" +
            "fa0a72656469732d62697473c040" +
            "fa056374696d65c26d08bc65" +
            "fa08757365642d6d656dc2b0c41000" +
            "fa08616f662d62617365c000" +
            "fff06e3bfec0ff5aa2"
    );
  }
}
