package network;

/**
 * HTTP URL
 */
public class Url {

    /**
     * Path to the file.
     */
    private String[] path;

    /**
     * Filename extracted from the url.
     */
    private String file;

    /**
     * File extension extracted from the url.
     */
    private String extension;

    /**
     * The plain url.
     */
    private String plain;

    /**
     * Constructor.
     * @param url Http url.
     */
    public Url(String url) {

        if (!url.startsWith("/")) {
            plain = "/";
            return;
        }

        plain = url;

        // strip query
        int posQuery;
        if ((posQuery = url.lastIndexOf('?')) != -1) {
            url = url.substring(0, posQuery);
        }

        if (url.endsWith("/")) {

            // the url is pointed to a folder.

            if (url.length() > 1) {
                String _url = url.substring(1, url.length() - 1);
                path = _url.split("/");
            }
            return;
        }

        // the url is pointed to a file.
        // parse path, filename and extension

        int posLastDot;
        if ((posLastDot = url.lastIndexOf('.')) != -1) {
            extension = url.substring(posLastDot + 1);
        } else {

            // read as folder
            String _url = url.substring(1, url.length());
            path = _url.split("/");
            return;
        }

        int posLastSlash;
        if ((posLastSlash = url.lastIndexOf('/')) != -1) {
            String _url = url.substring(1, posLastSlash + 1);
            path = _url.split("/");

            file = url.substring(posLastSlash + 1);
        }
    }

    /**
     * Url is pointed to a folder.
     * @return is pointed to a folder.
     */
    public boolean isFolder() {
        return extension == null || extension.equals("");
    }

    /**
     * Get path to the requested file.
     * @return path to the requested file.
     */
    public String[] getPath() {
        return path;
    }

    /**
     * Get the filename extracted from the url.
     * @return the filename extracted from the url.
     */
    public String getFile() {
        return file;
    }

    /**
     * Get the file extension extracted from the url.
     * @return file extension extracted from the url.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Get the plain url.
     * @return the plain url.
     */
    public String getPlain() {
        return plain;
    }
}
