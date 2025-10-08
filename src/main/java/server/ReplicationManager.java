package server;

import models.RespCommand;
import utility.RespUtility;
import java.io.*;
import java.net.Socket;

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

        RespCommand pingCommand = new RespCommand("PING");
        System.out.println("[REPL] Sending PING to master for handshake..."+pingCommand.getStringRepresentation());
        System.out.println(RespUtility.serializeCommand(pingCommand));
        writer.write(RespUtility.serializeCommand(pingCommand));
        writer.flush();
      } catch (IOException e) {
        System.out.println("[REPL] Error during replication handshake: " + e.getMessage());
      }
    }, "replication-slave-handshake");

    t.setDaemon(true);
    t.start();
  }
}

