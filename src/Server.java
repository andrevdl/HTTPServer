import files.FileManager;
import network.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    /**
     * Default HTTP port for communication.
     */
    private static final int PORT = 80;

    public static void main(String[] args) {
        new Server().execute();
    }

    /**
     * Execute the HTTP service.
     */
    public void execute() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            FileManager fileManager = new FileManager("config/hosts.txt");

            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println("Connection");

                Client client = new Client(socket, fileManager);
                client.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
