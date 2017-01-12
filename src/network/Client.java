package network;

import files.FileManager;
import files.rules.RuleSet;

import java.io.*;
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

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);

            Request request = new Request(reader);
            request.parse();

            try {
                if (!isSupportedVerb(request)) {
                    new Response(request, Response.METHOD_NOT_ALLOWED).send(writer);
                    return;
                }

                if (!fileManager.hostExists(request.getHost())) {
                    request.setHost(fileManager.getDefaultHost());
                }

                // read files.rules
                RuleSet ruleSet = fileManager.getRootRuleSet(request.getHost());
//            ruleSet.probe();

                // apply files.rules

                if (!request.getUrl().isFolder() && !Mime.isSupported(request.getUrl().getExtension())) {
                    new Response(request, Response.UNSUPPORTED_MEDIA_TYPE).send(writer);
                    return;
                }

                // get file
                FileManager.HttpFile file = fileManager.getFile(request.getHost(), request.getUrl());
                if (file == null) {
                    new Response(request, Response.FILE_NOT_FOUND).send(writer);
                } else {
                    new Response(request, file).send(writer);
                }
            } catch (Exception e) {
                System.err.println("500 error raised");
                new Response(null, Response.INTERNAL_ERROR).send(writer);
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            try {
                socket.close();
            } catch (Exception e) {
                //
            }
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
