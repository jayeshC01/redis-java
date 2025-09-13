package db;

import models.DataStoreValue;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
  public static final Map<String, DataStoreValue> store = new ConcurrentHashMap<>();
  public static final Map<String, Queue<Thread>> waiterThreads = new ConcurrentHashMap<>();
  public static final Map<String, String> configs = new ConcurrentHashMap<>();

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

  public static void notifyWaiter(String key) {
    Queue<Thread> queue = waiterThreads.get(key);
    if (queue != null) {
      Thread lock = queue.poll();
      if (lock != null) {
        synchronized (lock) {
          lock.notify();
        }
      }
      if (queue.isEmpty()) {
        waiterThreads.remove(key, queue);
      }
    }
  }

  public static void addWaiter(String key, Thread thread) {
    waiterThreads.computeIfAbsent(key, k -> new LinkedList<>()).add(thread);
  }

  public static Queue<Thread> getWaiters(String key) {
    return waiterThreads.get(key);
  }

  public static void cleanUpWaiter(String key) {
    waiterThreads.remove(key);
  }

  public static String getConfig(String config) {
    return configs.get(config);
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
