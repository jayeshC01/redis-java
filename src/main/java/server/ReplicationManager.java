package server;

import models.RespCommand;
import utility.RespUtility;
import java.io.*;
import java.net.Socket;
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

    Thread t = new Thread(() -> {
      try (Socket socket = new Socket(masterHost, masterPort);
           BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

        System.out.println("[REPL] Connected to master " + masterHost + ":" + masterPort);
        System.out.println("[REPL] Starting replication handshake...");

        RespCommand pingCommand = new RespCommand("PING");
        writer.write(RespUtility.serializeCommand(pingCommand));
        writer.flush();
        String pingResponse = reader.readLine();
        if (!pingResponse.equals("+PONG")) {
          System.out.println("[REPL] Error: Unexpected PING response from master: " + pingResponse);
          return;
        }

        RespCommand replConf1 = new RespCommand("REPLCONF",
            List.of("listening-port", String.valueOf(configs.get(PORT))));
        writer.write(RespUtility.serializeCommand(replConf1));
        writer.flush();
        String response2 = reader.readLine();
        if (!response2.equals("+OK")) {
          System.out.println("[REPL] Error: REPLCONF listening-port failed: " + response2);
          return;
        }

        //TODO: Hardcoded for now, will be dynamic later
        RespCommand replConf2 = new RespCommand("REPLCONF",
            List.of("capa", "psync2"));
        writer.write(RespUtility.serializeCommand(replConf2));
        writer.flush();
        String response3 = reader.readLine();
        if (!response3.equals("+OK")) {
          System.out.println("[REPL] Error: REPLCONF capa failed: " + response3);
          return;
        }

        RespCommand psyncCommand = new RespCommand("PSYNC", List.of("?", "-1"));
        writer.write(RespUtility.serializeCommand(psyncCommand));
        writer.flush();
      } catch (IOException e) {
        System.out.println("[REPL] Error during replication handshake: " + e.getMessage());
      }
    }, "replication-slave-handshake");

    t.setDaemon(true);
    t.start();
  }
}

