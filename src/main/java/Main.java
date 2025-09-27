import config.ConfigProcessor;
import db.DataStore;
import db.RdbLoader;
import server.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
    Socket clientSocket = null;
    int port = 6379;
    try(ServerSocket serverSocket = new ServerSocket(port)) {
      serverSocket.setReuseAddress(true);
      System.out.println("Redis server active on port "+port);
      ConfigProcessor.processAndStoreConfig(args);
      if(DataStore.configs.get("dbfilename") != null) {
        System.out.println("Datastore configs are : "+ DataStore.configs);
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
