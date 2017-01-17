package files.rules.result;

import network.Redirect;

/**
 * Alter the header for redirect.
 */
public class AltHeaderRedirect extends AltHeader {

    /**
     * Http request.
     */
    private Redirect redirect;

    /**
     * Constructor.
     * @param redirect Http request.
     */
    public AltHeaderRedirect(Redirect redirect) {
        super(new HttpHeaderProperty[] {new HttpHeaderProperty("Location", redirect.getDestination())});
        this.redirect = redirect;
    }

    /**
     * Get the HTTP code of the altered header.
     * @return the HTTP code of the altered header.
     */
    @Override
    public int getHttpCode() {
        return redirect.getCode();
    }
}
