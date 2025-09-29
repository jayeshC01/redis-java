package config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigProcessor {
  public static final Map<String, String> configs = new ConcurrentHashMap<>();

  public static void processAndStoreConfig(String[] args) {
    for(int i = 0; i<args.length; i++) {
      if(args[i].startsWith("--")){
        configs.put(args[i].substring(2), args[i+1]);
        i++;
      }
    }
  }
}
