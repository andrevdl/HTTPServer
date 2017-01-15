package network;

import files.FileManager;
import files.rules.RuleSet;
import files.rules.result.AltHeader;

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
                    new Response(Response.METHOD_NOT_ALLOWED).send(outputStream);
                    return;
                }

                if (!fileManager.hostExists(request.getHost())) {
                    request.setHost(fileManager.getDefaultHost());
                }

                // read files.rules
                RuleSet ruleSet = fileManager.getRootRuleSet(request.getHost());

                AltHeader altHeader = ruleSet.AltHeader(request);
                if (altHeader != null) {
                    Response.writeHeader(writer, altHeader);
                    return;
                }

                if (request.getUrl().isFolder() ? !ruleSet.supportMimeType(ruleSet.getGenericExtension()) : !ruleSet.supportMimeType(request.getUrl().getExtension())) {
                    new Response(Response.UNSUPPORTED_MEDIA_TYPE).send(outputStream);
                    return;
                }

                // get file
                FileManager.HttpFile file = fileManager.getFile(request.getHost(), request.getUrl());

                if (file == null) {
                    new Response(Response.FILE_NOT_FOUND).send(outputStream);
                } else {
                    new Response(file).send(outputStream);
                }

            } catch (Exception e) {
                new Response(Response.INTERNAL_ERROR).send(outputStream);
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
