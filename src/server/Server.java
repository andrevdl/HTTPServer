package server;

import client.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by André on 30-11-2016.
 */
public class Server {
    private static final int PORT = 80;

    public void execute() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket socket = serverSocket.accept();

                Client client = new Client(socket);
                client.start();
            }
        } catch (IOException e) {
            //
        }
    }
}
