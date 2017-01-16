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
 *
 */
public class RuleSet {

    /**
     *
     */
    private File file;

    /**
     *
     */
    private HashMap<String, Mime> mimeTypes;

    /**
     *
     */
    private HashSet<String> deniedFiles;

    /**
     *
     */
    private HashSet<String> deniedFolders;

    /**
     *
     */
    private String index;

    /**
     *
     */
    private HashMap<String, Redirect> redirects;

    /**
     *
     */
    private BasicAuth auth;

    /**
     *
     */
    private boolean root;

    /**
     *
     * @param file
     */
    public RuleSet(File file) {
        this.file = file;

        mimeTypes = new HashMap<>();
        init();
    }

    /**
     *
     * @param file
     * @param prev
     * @param root
     */
    public RuleSet(File file, RuleSet prev, boolean root) {
        this.file = file;
        this.root = root;

        mimeTypes = new HashMap<>(prev.mimeTypes);
        index = prev.index;
        init();
    }

    /**
     *
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
     *
     * @param request
     * @return
     */
    public AltHeader AltHeader(Request request) {
        if (auth != null) {
            AltHeaderAuth header = auth.probe(request);
            if (header != null) {
                return header;
            }
        }

        if (redirects.containsKey(request.getUrl().getPlain())) {
            return new AltHeaderRedirect(redirects.get(request.getUrl().getPlain()));
        }

        return null;
    }

    /**
     *
     * @param file
     * @return
     */
    public boolean index(File file) {
        return file.isFile() ? !deniedFiles.contains(file.getName()) : !deniedFolders.contains(file.getName());
    }

    /**
     *
     * @param ext
     * @return
     */
    public Mime getMimeType(String ext) {
        return mimeTypes.get(ext);
    }

    /**
     *
     * @param ext
     * @return
     */
    public boolean supportMimeType(String ext) {
        return mimeTypes.get(ext) != null;
    }

    /**
     *
     * @param file
     * @return
     */
    public boolean isGenericFile(File file) {
        String index = "index." + this.index;
        return index.equals(file.getName());
    }

    /**
     *
     * @return
     */
    public String getGenericExtension() {
        return index;
    }

    /**
     *
     */
    private class RuleConfigReader implements ConfigReaderListener {

        /**
         *
         * @param name Name of the property.
         * @param args The amount of arguments included the name argument.
         * @return
         */
        @Override
        public boolean allowedProperty(String name, int args) {
            switch (name) {
                case "redirect":
                    return args == 4;
                case "auth":
                    return args == 2;
                case "allow":
                    return args >= 3 && args <= 5;
                case "deny":
                    return args == 3;
                case "index":
                    return args == 2;
            }
            return false;
        }

        /**
         *
         * @param args Arguments of the property.
         * @return
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
         *
         * @param args
         * @return
         */
        private boolean parseRedirect(String[] args) {
            if (!root)
                return true;

            redirects.put(args[2], new Redirect(Integer.parseInt(args[1]), args[2], args[3]));
            return true;
        }

        /**
         *
         * @param args
         * @return
         */
        private boolean parseAuth(String[] args) {
            if (!root)
                return true;

            try {
                if (!args[1].startsWith("/"))
                    args[1] = "/" + args[1];

                auth = new BasicAuth(new File(file.getParent() + args[1]));
            } catch (Exception e) {
                return false;
            }

            return true;
        }

        /**
         *
         * @param args
         * @return
         */
        private boolean parseAllow(String[] args) {
            String type = args[1];
            if (type.equals("type") && args.length == 5) {
                String binary = args[4];
                if (!binary.equals("1") && !binary.equals("0"))
                    return false;

                mimeTypes.put(args[2], new Mime(args[2], args[3], binary.equals("1")));
            } else if (type.equals("folder")) {
                deniedFolders.remove(args[2]);
            } else if (type.equals("file")) {
                deniedFiles.remove(args[2]);
            }

            return false;
        }

        /**
         *
         * @param args
         * @return
         */
        private boolean parseDeny(String[] args) {
            String type = args[1];
            switch (type) {
                case "type":
                    mimeTypes.remove(args[2]);
                    break;
                case "folder":
                    deniedFolders.add(args[2]);
                    break;
                case "file":
                    deniedFiles.add(args[2]);
                    break;
            }

            return false;
        }

        /**
         *
         * @param args
         * @return
         */
        private boolean parseIndex(String[] args) {
            index = args[1];
            return true;
        }
    }
}
