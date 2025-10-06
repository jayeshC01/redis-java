package config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigProcessor {
  public static final Map<String, String> configs = new ConcurrentHashMap<>();

  public static void processAndStoreConfig(String[] args) {
    for(int i = 0; i<args.length; i++) {
      if(args[i].startsWith("--")){
        if(args[i].substring(2).equalsIgnoreCase("replicaOf")) {
          configs.put("role", "slave");
          i++;
          System.out.println("Testing "+args[i]);
          if(args[i].equalsIgnoreCase("localhost")) {
            configs.put("masterHost", "127.0.0.1");
          }
          String[] replicaString = args[i].split("\\s+");
          configs.put("masterHost", replicaString[0]);
          configs.put("masterPort", replicaString[1]);
          continue;
        }
        configs.put(args[i].substring(2), args[i+1]);
        i++;
      }
    }
    if(!configs.containsKey("role")) {
      configs.put("role", "master");
    }
  }
}
