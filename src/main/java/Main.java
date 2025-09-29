import config.ConfigProcessor;
import db.DataStore;
import db.RdbLoader;
import server.ClientHandler;
import static config.ConfigProcessor.configs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
    Socket clientSocket = null;
    ConfigProcessor.processAndStoreConfig(args);
    final int DEFAULT_PORT = 6379;
    int port = configs.get("port") != null ?
        Integer.parseInt(configs.get("port")) : DEFAULT_PORT;

    try(ServerSocket serverSocket = new ServerSocket(port)) {
      serverSocket.setReuseAddress(true);
      System.out.println("Redis server active on port "+port);
      if(configs.get("dbfilename") != null && configs.get("dir") != null) {
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
