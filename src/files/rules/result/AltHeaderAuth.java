package files.rules.result;

import network.Request;

/**
 *
 */
public class AltHeaderAuth extends AltHeader {

    /**
     *
     * @param request
     */
    public AltHeaderAuth(Request request) {
        super(new HttpHeaderProperty[] { new HttpHeaderProperty("WWW-Authenticate", "Basic realm=\"" + request.getHost() + "\"") });
    }

    /**
     * Get the HTTP code of the altered header.
     * @return the HTTP code of the altered header.
     */
    @Override
    public int getHttpCode() {
        return 401;
    }
}
