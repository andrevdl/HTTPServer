package files.rules.result;

import network.Request;

public class AltHeaderAuth extends AltHeader {

    public AltHeaderAuth(Request request) {
        super(new HttpHeaderProperty[] { new HttpHeaderProperty("WWW-Authenticate", "Basic realm=\"" + request.getHost() + "\"") });
    }

    @Override
    public int getHttpCode() {
        return 401;
    }
}
