package config;

import db.DataStore;

public class ConfigProcessor {

  public static void processAndStoreConfig(String[] args) {
    for(int i = 0; i<args.length; i++) {
      if(args[i].startsWith("--")){
        DataStore.configs.put(args[i].substring(2), args[i+1]);
        i++;
      }
    }
  }
}
