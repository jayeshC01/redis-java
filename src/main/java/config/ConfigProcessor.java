package config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigProcessor {
  public static final Map<String, String> configs = new ConcurrentHashMap<>();

  public static void processAndStoreConfig(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (!args[i].startsWith("--")) continue;

      String key = args[i].substring(2);
      if (key.equalsIgnoreCase("replicaOf")) {
        configs.put("role", "slave");
        if (++i < args.length) {
          String[] replicaInfo = args[i].split("\\s+");
          String host = replicaInfo[0].equalsIgnoreCase("localhost") ? "127.0.0.1" : replicaInfo[0];
          configs.put("masterHost", host);
          if (replicaInfo.length > 1) {
            configs.put("masterPort", replicaInfo[1]);
          }
        }
      } else {
        if (i + 1 < args.length) {
          configs.put(key, args[++i]);
        }
      }
    }
    configs.putIfAbsent("role", "master"); // Default role is master
    configureMasterSlaveDefaults();
  }

  private static void configureMasterSlaveDefaults() {
    if ("master".equalsIgnoreCase(configs.get("role"))) {
      configs.put("master_replid", "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb");
      configs.put("master_repl_offset", "0");
    }
  }
}
