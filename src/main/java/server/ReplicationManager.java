package server;

import models.RespCommand;
import utility.RespUtility;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import static config.ConfigProcessor.configs;
import static utility.Constants.*;

public class ReplicationManager {

  public static void startSlaveReplication() {
    String masterHost = configs.get(MASTER_HOST);
    String masterPortStr = configs.get(MASTER_PORT);
    if (masterHost == null || masterHost.isBlank()) {
      System.out.println("[REPL] master host not configured, cannot start replication.");
      return;
    }
    int masterPort = !masterPortStr.isEmpty() ? Integer.parseInt(masterPortStr) : DEFAULT_PORT;
    System.out.println("[REPL] Starting replication with master " + masterHost + ":" + masterPort);

    Thread t = new Thread(() ->
        performSlaveReplicationHandshake(masterHost, masterPort),
        "replication-slave-handshake");
    t.setDaemon(true);
    t.start();
  }

  private static void performSlaveReplicationHandshake(String masterHost, int masterPort) {
    try (Socket socket = new Socket(masterHost, masterPort);
         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

      System.out.println("[REPL] Connected to master " + masterHost + ":" + masterPort);
      System.out.println("[REPL] Starting replication handshake...");
      Boolean handShakePerform = initiateHandshake(reader, writer);
      if(handShakePerform) {
        System.out.println("[REPL] Handshake part completed, waiting for RDB file...");
      } else {
        System.out.println("[REPL] Handshake failed...");
        return;
      }
      readRDBFile(socket.getInputStream());
    } catch (IOException e) {
      System.out.println("[REPL] Error during replication handshake: " + e.getMessage());
    }
  }


  private static Boolean initiateHandshake(BufferedReader reader, BufferedWriter writer) throws IOException {
    RespCommand pingCommand = new RespCommand("PING");
    writer.write(RespUtility.serializeCommand(pingCommand));
    writer.flush();
    String pingResponse = reader.readLine();
    if (!pingResponse.equals("+PONG")) {
      System.out.println("[REPL] Error: Unexpected PING response from master: " + pingResponse);
      return false;
    }

    //Handshake Part 2.A: REPLCONF
    RespCommand replConf1 = new RespCommand("REPLCONF",
        List.of("listening-port", String.valueOf(configs.get(PORT))));
    writer.write(RespUtility.serializeCommand(replConf1));
    writer.flush();
    String response2 = reader.readLine();
    if (!response2.equals("+OK")) {
      System.out.println("[REPL] Error: REPLCONF listening-port failed: " + response2);
      return false;
    }

    //Handshake Part 2.B: REPLCONF CAPA
    //TODO: Hardcoded for now, will be dynamic later
    RespCommand replConf2 = new RespCommand("REPLCONF",
        List.of("capa", "psync2"));
    writer.write(RespUtility.serializeCommand(replConf2));
    writer.flush();
    String response3 = reader.readLine();
    if (!response3.equals("+OK")) {
      System.out.println("[REPL] Error: REPLCONF capa failed: " + response3);
      return false;
    }

    //Handshake Part 3: PSYNC
    RespCommand psyncCommand = new RespCommand("PSYNC", List.of("?", "-1"));
    writer.write(RespUtility.serializeCommand(psyncCommand));
    writer.flush();
    String response4 = reader.readLine();
    if (!response4.startsWith("+FULLRESYNC")) {
      System.out.println("[REPL] Error: Invalid FULLRESYNC response: " + response4);
      return false;
    }
    return true;
  }

  private static void readRDBFile(InputStream inputStream) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

      String header = reader.readLine();
      if (!header.startsWith("$")) {
        System.out.println("[REPL] Error: Invalid RDB length line: " + header);
        return;
      }
      int rdbLength = Integer.parseInt(header.substring(1));
      System.out.println("[REPL] RDB file length: " + rdbLength);

      byte[] rdbData = inputStream.readNBytes(rdbLength);
      if (rdbData.length < rdbLength) {
        throw new IOException("Incomplete RDB file: expected " + rdbLength + " bytes but got " + rdbData.length);
      }
      System.out.println("[REPL] Successfully read RDB file of length " + Arrays.toString(rdbData));
    }
    catch (IOException e) {
      System.out.println("[REPL] Error reading RDB file: " + e.getMessage());
    } catch (NumberFormatException e) {
      System.out.println("[REPL] Error: Invalid RDB length format: " + e.getMessage());
    }
  }
}

