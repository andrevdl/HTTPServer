package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

public class Request {
    private String verb;
    private Url url;
    private String protocol;
    private HashMap<String, String> header;
    private String body;

    private BufferedReader reader;

    public Request(BufferedReader reader) {
        this.reader = reader;
    }

    public void parse() throws IOException {
        boolean first = true;
        boolean body = false;

        String line;
        while ((line = reader.readLine()) != null) {
            if (first) {
                first = false;
                parseInitLine(line);
            }

            if (line.equals(" ")) {
                body = true;
                continue;
            }

            if (line.equals("")) {
                break;
            }

            if (!body) {
                parseArguments(line);
            } else {
                // later use
            }
        }
    }

    private void parseInitLine(String line) {
        String[] parts = line.split(" ");
        verb = parts[0];
        url = new Url(parts[1]);
        protocol = parts[2];
    }

    private void parseArguments(String line) {

    }

    public String getVerb() {
        return verb;
    }

    public Url getUrl() {
        return url;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return "";
    }
}
