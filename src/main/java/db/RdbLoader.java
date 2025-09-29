package db;

import models.DataStoreValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class RdbLoader {
  public static void loadDatabase() throws IOException {
    String dir = DataStore.configs.get("dir");
    String dbFileName = DataStore.configs.get("dbfilename");
    System.out.println("Datastore configs fetched :"+dir+" file:"+dbFileName);

    // File file = new File("/Users/jayeshc/XProjects/codecrafters-redis-java/src/main/java/db/dbFile.rdb");
    File file = new File(dir, dbFileName);
    System.out.println("Fetched File : "+file.getName());
    if (!file.exists()) {
      System.out.println("RDB file not found at " + file.getAbsolutePath() +
          ". Starting with empty database.");
      return;
    }

    try (FileInputStream fis = new FileInputStream(file)) {
      String header = new String(fis.readNBytes(9), StandardCharsets.UTF_8);
      System.out.println("HEADER : "+header);
      if (!header.startsWith("REDIS")) {
        System.out.println("Invalid RDB header. Starting with empty DB");
        return;
      }

      int data;
      long expiry = 0;
      while ((data = fis.read()) != -1) {
        System.out.println("Data :" + data);
        if (data == 0xFF) {
          System.out.println("End of RDB file reached");
          break;
        }

        if (data == 0xFA) {
          System.out.println("Inside metadata section");
          skipMetadata(fis);
          continue;
        }
        if (data == 0XFE) {
          processDBSection(fis);
          continue;
        }

        if (data == 0xFC) { // expiry in milliseconds
          byte[] expiryBytes = fis.readNBytes(8);
          expiry = bytesToLong(expiryBytes);
          System.out.println("key expiry (ms): " + expiry);
          continue;
        } else if (data == 0xFD) { // expiry in seconds
          byte[] expiryBytes = fis.readNBytes(4);
          expiry = bytesToLong(expiryBytes) * 1000;
          System.out.println("Key expiry (s->ms): " + expiry);
          continue;
        }

        int keyLength = readLength(fis, Optional.empty());
        String keyValue = readData(fis, keyLength);
        int dataLength = readLength(fis, Optional.empty());
        String dataValue = readData(fis, dataLength);
        System.out.println("Key: " + keyValue + "    Value: " + dataValue);
        if (expiry != 0) {
          DataStore.store.put(keyValue, new DataStoreValue(dataValue, expiry));
          expiry = 0;
        } else {
          DataStore.store.put(keyValue, new DataStoreValue(dataValue));
        }
      }
    } catch (IOException e) {
      System.out.println("Error Loading DB. Starting with empty DB");
      System.out.println("Error: "+e.getMessage());
      System.out.println("Stack Trace: " + Arrays.toString(e.getStackTrace()));
    }
  }

  private static void processDBSection(FileInputStream fis) throws IOException {
    int dbNumber = readLength(fis, Optional.empty());
    System.out.println("Switching to DB: " + dbNumber);
    int opcode = fis.read();
    if (opcode == 0xFB) {
      int hashTableSize = readLength(fis, Optional.empty());
      int expiresSize = readLength(fis, Optional.empty());
      System.out.println("Hash table size: " + hashTableSize + ", expires: " + expiresSize);
    }
  }

  private static void skipMetadata(FileInputStream fis) throws IOException {
    while (true) {
      int data = fis.read();
      if (data == 0xFE) {
        System.out.println("Exiting skipmetadata");
        processDBSection(fis);
        return;
      }

      if(data == 0xFA) {
        continue;
      }
      int length = readLength(fis, Optional.of(data));
      String d = readData(fis, length);
      System.out.println("The skipped data "+d);
    }
  }

  private static int readLength(FileInputStream fis, Optional<Integer> df) throws IOException {
    AtomicInteger data = new AtomicInteger();
    df.ifPresentOrElse(d -> {
          System.out.println("Data from optional :"+d);
          data.set(d);
        },
        () -> {
          try {
            data.set(fis.read());
            System.out.println("Data from read :"+data);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    if (data.get() == -1) throw new IOException("Unexpected EOF");
    int lengthEncoding = (data.get() >>> 6) & 0x03;
    System.out.println("Length Encoding:"+lengthEncoding);
    switch (lengthEncoding) {
      case 0:
        System.out.println("Returning Length : "+ (data.get() & 0x3F));
        return data.get() & 0x3F;
      case 1: {
        int last6 = data.get() & 0x3F;
        int b2 = fis.read();
        System.out.println("Returning Length case 1 : "+ ((last6 << 8) | b2));
        return ((last6 << 8) | b2);
      }
      case 2: {
        byte[] b2_4 = fis.readNBytes(4);
        int last6 = data.get() & 0x3F;
        int w = (last6 << 2);
        for (int d : b2_4) {
          w = (w << 8) | (d & 0xFF);
        }
        return w;
      }
      case 3: {
        int subtype = data.get() & 0x3F;
        return switch (subtype) {
          case 0 -> 1;
          case 1 -> 2;
          case 2 -> 3;
          case 3 -> throw new IOException("Special Encoding - subtype 3 not implemented");
          default -> throw new IOException("Invalid Type - Syntax Error");
        };
      }
    }
    throw new IOException("Invalid Type - Syntax Error");
  }


  private static String readData(FileInputStream fis, int length) throws IOException {
    byte[] bytes = fis.readNBytes(length);

    String data = new String(bytes, StandardCharsets.UTF_8);
    System.out.println("ReadString data :"+data);
    return data;
  }

  private static long bytesToLong(byte[] bytes) {
    long value = 0;
    for (int i = 0; i < bytes.length; i++) {
      value |= ((long)(bytes[i] & 0xFF)) << (8 * i);
    }
    return value;
  }
}
