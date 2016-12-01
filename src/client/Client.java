package client;

import server.FileManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client extends Thread {

    private Socket socket;
    private FileManager fileManager;
    private static final String[] supportedVerbs = new String[] { "GET" };

    public Client(Socket socket, FileManager fileManager) {
        this.socket = socket;
        this.fileManager = fileManager;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            Request request = new Request(reader);
            request.parse();

            if (!isSupportedVerb(request)) {
                // return error
                return;
            }

            String path = fileManager.getPath(request.getHost());
            if (path == null) {
                // return error
                return;
            }
        } catch (IOException e) {
            //
        }
    }

    public boolean isSupportedVerb(Request request) {
        for (String item : supportedVerbs) {
            if (item.equals(request.getVerb())) {
                return true;
            }
        }
        return false;
    }
}
