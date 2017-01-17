package files.rules.result;

/**
 * Http header property used for {@link AltHeader} objects.
 */
public class HttpHeaderProperty {

    /**
     * Key of the Http header property.
     */
    private String key;

    /**
     * Value of the Http header property.
     */
    private String value;

    /**
     * Constructor.
     * @param key Key of the Http header property.
     * @param value Value of the Http header property.
     */
    public HttpHeaderProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s: %s\r\n", key, value);
    }
}
