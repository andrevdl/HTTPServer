package network;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Url {
    private String path;
    private String extension;

    public Url(String url) {
        Pattern pattern = Pattern.compile("(?:/[\\w%]+)+(?:\\.([\\w]+))?$");
        Matcher m = pattern.matcher(url);

        while (m.find()) {
            path = m.group(0);
            extension = m.group(1);
        }
    }

    public boolean isFolder() {
        return extension == null || extension.equals("");
    }

    public String getPath() {
        return path;
    }

    public String getExtension() {
        return extension;
    }
}
