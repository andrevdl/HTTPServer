package files.rules.result;

import java.io.PrintWriter;

/**
 * Created altered header object
 * with extra properties, that will be placed into the header.
 */
public abstract class AltHeader {

    /**
     * Extra properties.
     */
    private HttpHeaderProperty[] properties;

    /**
     * Constructor.
     * @param properties Extra properties
     */
    public AltHeader(HttpHeaderProperty[] properties) {
        this.properties = properties;
    }

    /**
     * Get the HTTP code of the altered header.
     * @return the HTTP code of the altered header.
     */
    public abstract int getHttpCode();

    /**
     * Write the properties into the writer.
     * The writer is mostly coming from the {@link network.Response#writeHeader(PrintWriter, AltHeader)} method.
     * @param writer Writer.
     */
    public void getHeader(PrintWriter writer) {
        for (HttpHeaderProperty property : properties) {
            writer.print(property);
        }
    }
}
