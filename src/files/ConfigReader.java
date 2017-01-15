package files;

import java.io.*;
import java.nio.charset.Charset;

public class ConfigReader {

    private BufferedReader reader;
    private ConfigReaderListener listener;

    public ConfigReader(File file, ConfigReaderListener listener) throws IOException {
        InputStream stream = new FileInputStream(file);
        InputStreamReader streamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
        reader = new BufferedReader(streamReader);

        this.listener = listener;
    }

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
