import java.lang.*;
import java.net.Socket;
import java.io.IOException;

class ClientHandler implements Runnable  {
  private final Socket clientSocket;

  public ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public void run() {
    try {
      while (true) {
        // Read input from client.
        byte[] input = new byte[1024];
        clientSocket.getInputStream().read(input);
        String inputString = new String(input).trim();
        System.out.println("Received: " + inputString);
        clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
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
