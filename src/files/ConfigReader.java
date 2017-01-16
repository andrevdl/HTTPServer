package files;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Read a config file.
 */
public class ConfigReader {

    /**
     * Buffered reader.
     */
    private BufferedReader reader;

    /**
     * Listener of the config reader.
     */
    private ConfigReaderListener listener;

    /**
     * Constructor.
     * @param file File to read
     * @param listener Listener of the config reader.
     * @throws IOException Exception.
     */
    public ConfigReader(File file, ConfigReaderListener listener) throws IOException {
        InputStream stream = new FileInputStream(file);
        InputStreamReader streamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
        reader = new BufferedReader(streamReader);

        this.listener = listener;
    }

    /**
     * Read the file.
     * Read each single line and skipping the lines started with #.
     * Send the read lines to the listener.
     * @throws IOException Exception.
     */
    public void readFile() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) {
                String[] parts = line.split(" ");

                if (listener.allowedProperty(parts[0], parts.length)) {
                    listener.onReadProperty(parts);
                }
            }
        }
    }
}
