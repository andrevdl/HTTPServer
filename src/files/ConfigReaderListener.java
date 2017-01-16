package files;

/**
 * Listener of the config reader.
 * Used for parsing the config file to internal data structures.
 */
public interface ConfigReaderListener {

    /**
     * Given property is allowed or not.
     * A property is one line in the config file.
     *
     * Exclude from the lines, that starts with hash tag.
     * These lines will be interpreted as comment lines.
     *
     * Values of the property are split by spaces.
     * The name is the first value of property.
     * The others can be see as arguments for the given name.
     * @param name Name of the property.
     * @param args The amount of arguments included the name argument.
     * @return Given property is allowed or not.
     */
    boolean allowedProperty(String name, int args);

    /**
     * Read and parse the given property.
     * @param args Arguments of the property.
     * @return The given property is correct.
     */
    boolean onReadProperty(String[] args);
}
