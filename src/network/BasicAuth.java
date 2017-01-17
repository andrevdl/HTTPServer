package network;

import files.rules.result.AltHeaderAuth;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;

/**
 * Handle basic authorization.
 */
public class BasicAuth {

    /**
     * Credentials table (username/password)
     */
    private HashMap<String, String> users = new HashMap<>();

    /**
     * Constructor.
     * @param file File with credentials table (username/password).
     * @throws Exception Exception
     */
    public BasicAuth(File file) throws Exception {
        users = new HashMap<>();

        InputStream stream = new FileInputStream(file);
        InputStreamReader streamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
        BufferedReader reader = new BufferedReader(streamReader);

        // parse the credentials table file
        String line;
        while ((line = reader.readLine()) != null) {
            String[] pair = line.split(" ");
            if (pair.length != 2)
                throw new Exception();

            users.put(pair[0], pair[1]);
        }
    }

    /**
     * Probe of the request is authorised.
     * If true, check the send data for correctness and return null.
     * Else return {@link AltHeaderAuth},
     * so that the client will be requested to authorise himself.
     * @param request Http Request.
     * @return If authorised, returns null else a {@link AltHeaderAuth} object.
     */
    public AltHeaderAuth probe(Request request) {
        String value = request.getArgument("Authorization");
        if (value != null) {
            String[] parts = value.split(" ");

            // parse the authorization header and lookup against the credentials table.
            if (parts.length == 2 && parts[0].equals("Basic")) {
                byte[] keyPair = Base64.getDecoder().decode(parts[1]);

                int index = 0;
                for (int i = 0; i < keyPair.length; i++) {
                    if (keyPair[i] == ':') {
                        index = i;
                        break;
                    }
                }

                if (index != 0) {
                    String username = new String(keyPair, 0, index);
                    String password = new String(keyPair, index + 1, keyPair.length - index - 1);

                    String u = users.get(username);
                    if (u != null && u.equals(password)) {
                        return null;
                    }
                }
            }
        }

        return new AltHeaderAuth(request);
    }
}
