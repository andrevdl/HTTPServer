package server;

import client.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 80;

    public void execute() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            FileManager fileManager = new FileManager("config/hosts.txt");


            while (true) {
                Socket socket = serverSocket.accept();

                Client client = new Client(socket, fileManager);
                client.start();
            }
        } catch (IOException e) {
            //
        }
    }
}
