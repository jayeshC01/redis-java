package server;

import models.RespCommand;
import processors.CommandProcessor;
import utility.RespUtility;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
  private final Socket clientSocket;
  private final CommandProcessor commandProcessor;

  public ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
    this.commandProcessor = new CommandProcessor();
  }

  public void run() {
    try {
      BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(clientSocket.getInputStream()));
      BufferedWriter writer =
          new BufferedWriter(
              new OutputStreamWriter(clientSocket.getOutputStream()));
      while(true) {
        RespCommand cmd = RespUtility.parseRespCommand(reader);
        System.out.println("Executing Command: " + cmd.getStringRepresentation());
        String response = commandProcessor.processCommand(cmd);
        System.out.println("Response Send: " + response);
        writer.write(response);
        writer.flush();
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
      }
    }
  }
}
