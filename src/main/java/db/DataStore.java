package db;

import models.DataStoreValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
  public static final Map<String, DataStoreValue> store = new ConcurrentHashMap<>();

  public static DataStoreValue get(String key) {
    return store.get(key);
  }

  public static void put(String key, DataStoreValue value) {
    store.put(key, value);
  }

  public static boolean containsKey(String key) {
    return store.containsKey(key);
  }

  public static void remove(String key) {
    store.remove(key);
  }

  public static void printDataStore() {
    System.out.println("===== DataStore Snapshot =====");
    if (store.isEmpty()) {
      System.out.println("The data store is empty");
    } else {
      store.forEach((k, v) -> {
        String val;
        try {
          val = v.getAsString();
        } catch (Exception e) {
          val = v.toString();
        }
        System.out.println(k + " -> " + val);
      });
    }
    System.out.println("==============================");
  }
}
