package network;

import network.exception.MimeNotSupported;

public class Mime {
    private String extension;
    private String mimeType;

    private static final String[] supported = new String[] {
            "htm", "html", "js", "css", "txt", "json", "xml"
    };

    public Mime(String extension) throws MimeNotSupported {
        switch (extension) {
            case "htm":
            case "html":
                mimeType = "";
                break;
            case "js":
                mimeType = "";
                break;
            case "css":
                mimeType = "";
                break;
            case "txt":
                mimeType = "";
                break;
            case "json":
                mimeType = "";
                break;
            case "xml":
                mimeType = "";
                break;
            default:
                throw new MimeNotSupported();
        }

        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static boolean isSupported(String extension) {
        for (String item : supported) {
            if (item.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
