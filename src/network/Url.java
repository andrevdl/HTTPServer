package network;

public class Url {
    private String[] path;
    private String file;
    private String extension;
    private String plain;

    public Url(String url) {
        if (!url.startsWith("/")) {
            plain = "/";
            return;
        }

        plain = url;

        //strip query
        int posQuery;
        if ((posQuery = url.lastIndexOf('?')) != -1) {
            url = url.substring(0, posQuery);
        }

        if (url.endsWith("/")) {
            if (url.length() > 1) {
                String _url = url.substring(1, url.length() - 1);
                path = _url.split("/");
            }
            return;
        }

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

    public boolean isFolder() {
        return extension == null || extension.equals("");
    }

    public String[] getPath() {
        return path;
    }

    public String getFile() {
        return file;
    }

    public String getExtension() {
        return extension;
    }

    public String getPlain() {
        return plain;
    }
}
