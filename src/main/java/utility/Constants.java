package utility;

public class Constants {
  // Private constructor to prevent instantiation
  private  Constants() {}

  public static final String CRLF = "\r\n";

  //Port related constants
  public static final int DEFAULT_PORT = 6379;
  public static final String PORT = "port";

  //DB related constants
  public static final String DB_FILENAME = "dbfilename";
  public static final String DB_DIR = "dir";

  // Replication related constants
  public static final String ROLE = "role";
  public static final String ROLE_MASTER = "master";
  public static final String ROLE_SLAVE = "slave";
  public static final String INFO_SECTION_REPLICATION = "replication";
  public static final String MASTER_REPLID = "master_replid";
  public static final String MASTER_REPL_OFFSET = "master_repl_offset";
  public static final String MASTER_HOST = "masterHost";
  public static final String MASTER_PORT = "masterPort";
  public static final String MASTER_LINK_STATUS = "master_link_status";
  public static final String MASTER_LINK_UP = "up";
  public static final String REPLICA_FLAG = "replicaOf";
}
