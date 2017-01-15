package files.rules.result;

import network.Redirect;

public class AltHeaderRedirect extends AltHeader {

    private Redirect redirect;

    public AltHeaderRedirect(Redirect redirect) {
        super(new HttpHeaderProperty[] {new HttpHeaderProperty("Location", redirect.getDestination())});
        this.redirect = redirect;
    }

    @Override
    public int getHttpCode() {
        return redirect.getCode();
    }
}
