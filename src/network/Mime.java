package network;

/**
 * Mime type.
 */
public class Mime {

    /**
     * Extension of the mime type.
     */
    private String extension;

    /**
     * Mime type.
     */
    private String mime;

    /**
     * Mime is binary or text based.
     * If binary the boolean is true, else false.
     */
    private boolean binary;

    /**
     * Constructor.
     * @param extension Extension of the mime type
     * @param mime Mime type.
     * @param binary Mime binary or not.
     */
    public Mime(String extension, String mime, boolean binary) {
        this.extension = extension;
        this.mime = mime;
        this.binary = binary;
    }

    /**
     * Get the extension of the mime type.
     * @return the extension of the mime type.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Get the mime type.
     * @return the mime type.
     */
    public String getMime() {
        return mime;
    }

    /**
     * Checking of the mime is binary or not.
     * @return the mime is binary or not.
     */
    public boolean isBinary() {
        return binary;
    }

    /**
     * Get the default mime type.
     * @return the default mime type.
     */
    public static String getDefault() {
        return "text/plain";
    }
}
