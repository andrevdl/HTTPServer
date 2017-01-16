package network;

/**
 * Redirect object.
 */
public class Redirect {

    /**
     * Code of the redirect.
     */
    private int code;

    /**
     * Source of the redirect.
     */
    private String source;

    /**
     * Destination of the redirect.
     */
    private String destination;

    /**
     * Constructor.
     * @param code Code of the redirect.
     * @param source Source of the redirect.
     * @param destination Destination of the redirect.
     */
    public Redirect(int code, String source, String destination) {
        this.code = code;
        this.source = source;
        this.destination = destination;
    }

    /**
     * Get code of the redirect.
     * @return code of the redirect.
     */
    public int getCode() {
        return code;
    }

    /**
     * Get source of the redirect.
     * @return source of the redirect.
     */
    public String getSource() {
        return source;
    }

    /**
     * Get
     * @return destination of the redirect.
     */
    public String getDestination() {
        return destination;
    }
}
