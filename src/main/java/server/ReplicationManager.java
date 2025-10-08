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

        RespCommand pingCommand = new RespCommand("PING");
        System.out.println("[REPL] Sending PING to master for handshake..."+pingCommand.getStringRepresentation());
        writer.write(RespUtility.serializeCommand(pingCommand));
        writer.flush();
        System.out.println(reader.readLine());

        RespCommand replConf1 = new RespCommand("REPLCONF",
            List.of("listening-port", String.valueOf(configs.get(PORT))));

        //TODO: Hardcoded for now, will be dynamic later
        RespCommand replConf2 = new RespCommand("REPLCONF",
            List.of("capa", "psync2"));
        
        writer.write(RespUtility.serializeCommand(replConf1));
        writer.flush();
        writer.write(RespUtility.serializeCommand(replConf2));
        writer.flush();
      } catch (IOException e) {
        System.out.println("[REPL] Error during replication handshake: " + e.getMessage());
      }
    }, "replication-slave-handshake");

    t.setDaemon(true);
    t.start();
  }
}

