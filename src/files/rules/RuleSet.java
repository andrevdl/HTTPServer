package files.rules;

import files.ConfigReader;
import files.ConfigReaderListener;
import files.rules.result.AltHeader;
import files.rules.result.AltHeaderAuth;
import files.rules.result.AltHeaderRedirect;
import network.BasicAuth;
import network.Mime;
import network.Redirect;
import network.Request;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Rules settings for the folder where the associated file is placed.
 * Rules of given kind can have influence on sub folders.
 *
 * Rules describe follow things:
 * - Which files, folders, extensions would be indexed
 * - Which mime types are supported
 * - Which extension will be used for the index file
 * - Redirects (if the ruleSet is a root rules settings file)
 * - Authorization (if the ruleSet is a root rules settings file)
 */
public class RuleSet {

    /**
     * Rules settings file.
     */
    private File file;

    /**
     * Allowed mime types.
     * Has influence on sub folders.
     */
    private HashMap<String, Mime> mimeTypes;

    /**
     * Denied files.
     * Has non influence on sub folders.
     */
    private HashSet<String> deniedFiles;

    /**
     * Denied folders.
     * Has non influence on sub folders.
     */
    private HashSet<String> deniedFolders;

    /**
     * Denied extensions.
     * Has influence on sub folders.
     */
    private HashSet<String> deniedExtensions;

    /**
     * Index extension.
     * Has influence on sub folders.
     */
    private String index;

    /**
     * Redirects for this host
     */
    private HashMap<String, Redirect> redirects;

    /**
     * Basic authorization for this host.
     * Has influence on sub folders.
     */
    private BasicAuth auth;

    /**
     * Is this a root rules settings file.
     */
    private boolean root;

    /**
     * Constructor.
     * @param file Rules settings file.
     */
    public RuleSet(File file) {
        this.file = file;

        mimeTypes = new HashMap<>();
        deniedExtensions = new HashSet<>();
        init();
    }

    /**
     * Constructor.
     * @param file Rules settings file.
     * @param prev Previous rules settings file (Parent file)
     * @param root Is this a root rules settings file.
     */
    public RuleSet(File file, RuleSet prev, boolean root) {
        this.file = file;
        this.root = root;

        mimeTypes = new HashMap<>(prev.mimeTypes);
        deniedExtensions = new HashSet<>(prev.deniedExtensions);
        index = prev.index;
        init();
    }

    /**
     * Init the object, by reading the associated file.
     */
    private void init() {
        deniedFiles = new HashSet<>();
        deniedFolders = new HashSet<>();
        redirects = new HashMap<>();

        try {
            ConfigReader reader = new ConfigReader(file, new RuleConfigReader());
            reader.readFile();
        } catch (Exception e) {
            //
        }
    }

    /**
     * Get the altered header.
     * If the header mustn't be altered if will return null,
     * else the altered header object.
     * @param request Client Http request.
     * @return the altered header.
     */
    public AltHeader AltHeader(Request request) {
        if (redirects.containsKey(request.getUrl().getPlain())) {
            return new AltHeaderRedirect(redirects.get(request.getUrl().getPlain()));
        }

        if (auth != null) {
            AltHeaderAuth header = auth.probe(request);
            if (header != null) {
                return header;
            }
        }

        return null;
    }

    /**
     * Ask of the given file/folder must be indexed.
     * @param file file/folder to index.
     * @return file/folder must be indexed or not.
     */
    public boolean index(File file) {
        return file.isFile() ? !deniedFiles.contains(file.getName()) : !deniedFolders.contains(file.getName());
    }

    /**
     * Ask of the given extension must be indexed.
     * @param ext Extension to index.
     * @return extension must be indexed or not.
     */
    public boolean indexExtension(String ext) {
        return !deniedExtensions.contains(ext);
    }

    /**
     * Get the mime type of the given extension.
     * If not supported, it will return null.
     * @param ext Extension.
     * @return the mime type of the given extension.
     */
    public Mime getMimeType(String ext) {
        return mimeTypes.get(ext);
    }

    /**
     * Is the given extension supported.
     * @param ext Extension.
     * @return the given extension supported.
     */
    public boolean supportMimeType(String ext) {
        return mimeTypes.get(ext) != null;
    }

    /**
     * Is the given file the generic index file.
     * @param file File to check.
     * @return the given file the generic index file.
     */
    public boolean isGenericFile(File file) {
        String index = "index." + this.index;
        return index.equals(file.getName());
    }

    /**
     * Get the generic extension of the index file.
     * @return the generic extension of the index file.
     */
    public String getGenericExtension() {
        return index;
    }

    /**
     * Reader listener to parse config file.
     */
    private class RuleConfigReader implements ConfigReaderListener {

        /**
         * Check of the property is allowed.
         * @param name Name of the property.
         * @param args The amount of arguments included the name argument.
         * @return property is allowed
         */
        @Override
        public boolean allowedProperty(String name, int args) {
            switch (name) {
                case "redirect":
                    return args == 4;
                case "auth":
                    return args == 2;
                case "allow":
                    return args >= 3;
                case "deny":
                    return args >= 3;
                case "index":
                    return args == 2;
            }
            return false;
        }

        /**
         * Parse single property.
         * @param args Arguments of the property.
         * @return could be parsed.
         */
        @Override
        public boolean onReadProperty(String[] args) {
            switch (args[0]) {
                case "redirect":
                    return parseRedirect(args);
                case "auth":
                    return parseAuth(args);
                case "allow":
                    return parseAllow(args);
                case "deny":
                    return parseDeny(args);
                case "index":
                    return parseIndex(args);
            }
            return false;
        }

        /**
         * Parse the redirect.
         * @param args Arguments.
         * @return could be parsed.
         */
        private boolean parseRedirect(String[] args) {
            if (!root)
                return true;

            redirects.put(args[2], new Redirect(Integer.parseInt(args[1]), args[2], args[3]));
            return true;
        }

        /**
         * Parse the authorization.
         * @param args Arguments.
         * @return could be parsed.
         */
        private boolean parseAuth(String[] args) {
            if (!root)
                return true;

            try {
                String path = parseName(args, 1);

                if (!path.startsWith("/"))
                    path = "/" + path;

                auth = new BasicAuth(new File(file.getParent() + path));
            } catch (Exception e) {
                return false;
            }

            return true;
        }

        /**
         * Parse the allow property.
         * @param args Arguments.
         * @return could be parsed.
         */
        private boolean parseAllow(String[] args) {
            String type = args[1];
            if (type.equals("type") && args.length == 5) {
                String binary = args[4];
                if (!binary.equals("1") && !binary.equals("0"))
                    return false;

                mimeTypes.put(args[2], new Mime(args[2], args[3], binary.equals("1")));
                return true;
            } else if (type.equals("folder")) {
                deniedFolders.remove(parseName(args, 2));
                return true;
            } else if (type.equals("file")) {
                deniedFiles.remove(parseName(args, 2));
                return true;
            } else if (type.equals("ext")) {
                deniedExtensions.remove(args[2]);
                return true;
            }

            return false;
        }

        /**
         * Parse the deny property.
         * @param args Arguments.
         * @return could be parsed.
         */
        private boolean parseDeny(String[] args) {
            String type = args[1];
            switch (type) {
                case "type":
                    mimeTypes.remove(args[2]);
                    return true;
                case "folder":
                    deniedFolders.add(parseName(args, 2));
                    return true;
                case "file":
                    deniedFiles.add(parseName(args, 2));
                    return true;
                case "ext":
                    deniedExtensions.add(args[2]);
                    return true;
            }

            return false;
        }

        /**
         * Parse the last arguments as a concat name.
         * @param args Arguments.
         * @param start Start index.
         * @return concat name.
         */
        private String parseName(String[] args, int start) {
            String name = "";
            for (int i = start; i < args.length; i++) {
                name += i == args.length - 1 ? args[i] : args[i] + " ";
            }
            return name;
        }

        /**
         * Parse the index property.
         * @param args Arguments.
         * @return could be parsed.
         */
        private boolean parseIndex(String[] args) {
            index = args[1];
            return true;
        }
    }
}
