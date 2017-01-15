package files;

public interface ConfigReaderListener {

    boolean allowedProperty(String name, int args);

    boolean onReadProperty(String[] args);
}
