package files.rules.result;

/**
 *
 */
public class HttpHeaderProperty {

    /**
     *
     */
    private String key;

    /**
     *
     */
    private String value;

    /**
     *
     * @param key
     * @param value
     */
    public HttpHeaderProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("%s: %s\r\n", key, value);
    }
}
