package files.rules.result;

import network.Redirect;

/**
 *
 */
public class AltHeaderRedirect extends AltHeader {

    /**
     *
     */
    private Redirect redirect;

    /**
     *
     * @param redirect
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
