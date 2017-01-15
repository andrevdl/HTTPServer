package network;

public class Redirect {
    private int code;
    private String source;
    private String destination;

    public Redirect(int code, String source, String destination) {
        this.code = code;
        this.source = source;
        this.destination = destination;
    }

    public int getCode() {
        return code;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }
}
