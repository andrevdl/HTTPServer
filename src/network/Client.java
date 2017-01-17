package network;

import files.FileManager;
import files.rules.RuleSet;
import files.rules.result.AltHeader;

import java.io.*;
import java.net.Socket;

/**
 * Handle the client request in a own thread.
 * After sending data to the client, the thread will be closed.
 */
public class Client extends Thread {

    /**
     * Client socket.
     */
    private Socket socket;

    /**
     * File manager.
     */
    private FileManager fileManager;

    /**
     * List of supported verbs.
     */
    private static final String[] supportedVerbs = new String[] { "GET" };

    /**
     * Constructor.
     * @param socket Client socket.
     * @param fileManager File manager.
     */
    public Client(Socket socket, FileManager fileManager) {
        this.socket = socket;
        this.fileManager = fileManager;
    }

    /**
     * Lifetime of requesting data from the server.
     * Each step that is be needed for a request will here be handled.
     */
    @Override
    public void run() {
        try {

            // Handle the input stream from the client.
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Handle the output stream to the client.
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);

            // Parse the incoming request.
            Request request = new Request(reader);
            request.parse();

            try {

                // Check for verb support.
                if (!isSupportedVerb(request)) {
                    new Response(Response.METHOD_NOT_ALLOWED).send(outputStream);
                    return;
                }

                // If the requested host doesn't exists, use the default host.
                // The default host is the first entry in the host file.
                if (!fileManager.hostExists(request.getHost())) {
                    request.setHost(fileManager.getDefaultHost());
                }

                // Read the root rule settings file of this requested host.
                RuleSet ruleSet = fileManager.getRootRuleSet(request.getHost());

                // Checking of the header must be altered,
                // because of redirects, authorization or likely.
                // If true, send the header to client and end the lifecycle.
                AltHeader altHeader = ruleSet.AltHeader(request);
                if (altHeader != null) {
                    Response.writeHeader(writer, altHeader);
                    return;
                }

                // Check of the requested mime/content type is supported.
                if (!fileManager.hasSupportedMime(request.getHost(), request.getUrl())) {
                    new Response(Response.UNSUPPORTED_MEDIA_TYPE).send(outputStream);
                    return;
                }

                // Get the requested file
                FileManager.HttpFile file = fileManager.getFile(request.getHost(), request.getUrl());

                // Checking of the file is founded or not.
                // If founded, parse the file with correct header and send it to the client.
                // Else send a not found header.
                if (file != null) {
                    new Response(file).send(outputStream);
                } else {
                    new Response(Response.FILE_NOT_FOUND).send(outputStream);
                }

            } catch (Exception e) {

                // If some error happens send 505 header.
                new Response(Response.INTERNAL_ERROR).send(outputStream);
            } finally {

                // close in all cases the socket.
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

    /**
     * Checking of the requested verb type is supported.
     * @param request Request from the client.
     * @return Verb type is supported or not.
     */
    private boolean isSupportedVerb(Request request) {
        for (String item : supportedVerbs) {
            if (item.equals(request.getVerb())) {
                return true;
            }
        }
        return false;
    }
}
