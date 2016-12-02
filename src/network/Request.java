package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request {
    private String verb;
    private Url url;
    private String protocol;
    private HashMap<String, String> header;
    private String body;

    private BufferedReader reader;

    public Request(BufferedReader reader) {
        this.reader = reader;
        this.header = new HashMap<>();
    }

    public void parse() throws IOException {
        boolean first = true;
        boolean body = false;

        String line;
        while ((line = reader.readLine()) != null) {
            if (first) {
                first = false;
                parseInitLine(line);
                continue;
            } else if (line.equals(" ")) {
                body = true;
                continue;
            } else if (line.equals("")) {
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
        Pattern pattern = Pattern.compile("(\\S+): (.*)");
        Matcher m = pattern.matcher(line);

        while (m.find()) {
            header.put(m.group(1), m.group(2));
        }
    }

    public String getArgument(String key) {
        return header.get(key);
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
        return getArgument("Host");
    }
}
