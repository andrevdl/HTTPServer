package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP Request
 */
public class Request {

    /**
     * Verb of the request.
     */
    private String verb;

    /**
     * Url of the request.
     */
    private Url url;

    /**
     * Protocol of the request.
     */
    private String protocol;

    /**
     * Headers of the request.
     */
    private HashMap<String, String> header;

    /**
     * Body of the request.
     */
    private String body;

    /**
     * Reader to read the request.
     */
    private BufferedReader reader;

    /**
     * Constructor.
     * @param reader Reader to read the request.
     */
    public Request(BufferedReader reader) {
        this.reader = reader;
        this.header = new HashMap<>();
    }

    /**
     * Parse the received data as a request object.
     * @throws IOException Exception.
     */
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
                // need implementation if sending body will be supported.
            }
        }
    }

    /**
     * Parse the first line of the request.
     * @param line Data of the line.
     */
    private void parseInitLine(String line) {
        String[] parts = line.split(" ");
        verb = parts[0];
        url = new Url(parts[1]);
        protocol = parts[2];
    }

    /**
     * Parse header argument.
     * @param line Line to parse.
     */
    private void parseArguments(String line) {
        Pattern pattern = Pattern.compile("(\\S+): (.*)");
        Matcher m = pattern.matcher(line);

        while (m.find()) {
            header.put(m.group(1), m.group(2));
        }
    }

    /**
     * Get a header argument.
     * @param key Name of the argument.
     * @return requested header argument.
     */
    public String getArgument(String key) {
        return header.get(key);
    }

    /**
     * Get verb of the request.
     * @return verb of the request.
     */
    public String getVerb() {
        return verb;
    }

    /**
     * Get url of the request.
     * @return url of the request.
     */
    public Url getUrl() {
        return url;
    }

    /**
     * Get protocol of the request.
     * @return protocol of the request.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Get host argument of the request.
     * @return host argument of the request.
     */
    public String getHost() {
        return getArgument("Host");
    }

    /**
     * Set host argument of the request.
     * @param host hostname.
     */
    public void setHost(String host) {
        header.put("Host", host);
    }
}
