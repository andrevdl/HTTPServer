package network;

public class Mime {
    private String extension;
    private String mime;
    private boolean binary;

    public Mime(String extension, String mime, boolean binary) {
        this.extension = extension;
        this.mime = mime;
        this.binary = binary;
    }

    public String getExtension() {
        return extension;
    }

    public String getMime() {
        return mime;
    }

    public boolean isBinary() {
        return binary;
    }

    public static String getDefault() {
        return "text/plain";
    }
}
