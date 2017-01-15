package files.rules.result;

import java.io.PrintWriter;

public abstract class AltHeader {

    private HttpHeaderProperty[] properties;

    public AltHeader(HttpHeaderProperty[] properties) {
        this.properties = properties;
    }

    public abstract int getHttpCode();

    public void getHeader(PrintWriter writer) {
        for (HttpHeaderProperty property : properties) {
            writer.print(property);
        }
    }
}
