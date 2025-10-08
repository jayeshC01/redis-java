import static config.ConfigProcessor.configs;
import static utility.Constants.*;

import config.ConfigProcessor;
import db.DataStore;
import db.RdbLoader;
import server.ClientHandler;
import server.ReplicationManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
    Socket clientSocket = null;
    ConfigProcessor.processAndStoreConfig(args);

    int port = configs.get(PORT) != null ?
        Integer.parseInt(configs.get(PORT)) : DEFAULT_PORT;

    if (ROLE_SLAVE.equalsIgnoreCase(configs.get(ROLE))) {
      ReplicationManager.startSlaveReplication();
    }

    try(ServerSocket serverSocket = new ServerSocket(port)) {
      serverSocket.setReuseAddress(true);
      System.out.printf("[%s] Redis server active on port %d%n", configs.get(ROLE), port);
      if(configs.get(DB_FILENAME) != null && configs.get(DB_DIR) != null) {
        RdbLoader.loadDatabase();
      }
      DataStore.printDataStore();
      while(true) {
        // Wait for connection from client.
        clientSocket = serverSocket.accept();
        System.out.println("New client connected: " + clientSocket.getInetAddress());
        new Thread(new ClientHandler(clientSocket)).start();
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
