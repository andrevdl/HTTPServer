package network;

import files.FileManager;
import files.rules.RuleSet;
import network.exception.MimeNotSupported;

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

            if (!fileManager.hostExists(request.getHost())) {
                // return error
                return;
            }

            // read files.rules
            RuleSet ruleSet = fileManager.getRootRuleSet(request.getHost());
//            ruleSet.probe();

            // apply files.rules

            if (!request.getUrl().isFolder() && !Mime.isSupported(request.getUrl().getExtension())) {
                // return error
                return;
            }

            // get file
            FileManager.HttpFile file = fileManager.getFile(request.getHost(), request.getUrl());
            if (file == null) {
                // return error
                return;
            }

            Response response = new Response(request, file);
            response.send();
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
