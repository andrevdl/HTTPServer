package files;

import network.Mime;
import files.rules.RuleSet;
import network.Url;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Managing all files for all hosts.
 * Each host has a entry in the {@link FileManager#roots} hash map.
 * In a entry is Hierarchic data structure that respectively the folder structure.
 * In the structure all pointers to files are be saved. Each file is be saved as a {@link HttpFile}.
 *
 * If the software is running and a folder or file will be added to a folder, they won't be scanned by the {@link FileManager}.
 * When these will be requested by a client, they will be come a {@link network.Response#FILE_NOT_FOUND} error.
 * To add these to the structure, the software must be rebooted by the user of the software.
 *
 * Scanned files can be altered, but the names of files and the place of the files must be exactly the same as before the change,
 * else will be a {@link network.Response#FILE_NOT_FOUND} error responded by requesting the given file.
 * It is possible that the files will be locked by the application, if that is the case it is impossible the altered the files.
 *
 * It is possible to exclude files or folders from scanning.
 * These files and folders wont't be saved into the data structure.
 * If these will be requested the client will be {@link network.Response#FILE_NOT_FOUND} error response.
 * Files and folders can be excluded by the {@link RuleSet}.
 * These folders and files will never be locked by the software after the initialization.
 */
public class FileManager {

    /**
     * Default host, also used as fallback if the requested hosts doesn't exists.
     */
    private String defaultHost;

    /**
     * Hash map with the hosts and roots of it hosts with given file structure.
     */
    private HashMap<String, FolderNode> roots;

    /**
     * The base rule settings.
     * These rules settings will be append
     * to the root rule settings of the given hosts.
     */
    private RuleSet baseRuleSet;

    /**
     * Constructor.
     * @param path Path to the host file.
     */
    public FileManager(String path) {
        roots = new HashMap<>();

        try {
            baseRuleSet = new RuleSet(new File("config/rules.set"));

            ConfigReader reader = new ConfigReader(new File(path), new HostListener());
            reader.readFile();
        } catch (IOException e) {
            System.out.println("error");
        }
    }

    /**
     * Checking of the requested host exists.
     * @param host Requested host to check.
     * @return Host exists or not.
     */
    public boolean hostExists(String host) {
        return roots.containsKey(host);
    }

    /**
     * Get file from the file structure of the given host.
     * @param host Requested host.
     * @param url Url to the given file.
     * @return If the file exists, the file. Else it will return null.
     */
    public synchronized HttpFile getFile(String host, Url url) {
        FolderNode node = roots.get(host);
        return node != null ? node.getFile(url) : null;
    }

    /**
     * Get the root rule settings of the given host.
     * @param host Requested host.
     * @return Rule set of the host.
     */
    public synchronized RuleSet getRootRuleSet(String host) {
        FolderNode node = roots.get(host);
        return node != null ? node.getRuleSet() : null;
    }

    public synchronized boolean hasSupportedMime(String host, Url url) {
        FolderNode node = roots.get(host);
        return node != null ? node.hasSupportedMime(url) : baseRuleSet.supportMimeType(url.getExtension());
    }

    /**
     * Parse the host config file.
     */
    private class HostListener implements ConfigReaderListener {

        /**
         * The given line in the host file is allowed.
         * @param name Name of the property.
         * @param args The amount of arguments included the name argument.
         * @return Given property has 2 arguments or not.
         */
        @Override
        public boolean allowedProperty(String name, int args) {
            return args == 2;
        }

        /**
         * Parse the given property into the host hash map.
         * @param args Arguments of the property.
         * @return Given property is correct formatted.
         */
        @Override
        public boolean onReadProperty(String[] args) {
            roots.put(args[0], new FolderNode(args[1], baseRuleSet));

            if (defaultHost == null) {
                defaultHost = args[0];
            }

            return true;
        }
    }

    /**
     * Virtual folder that holds information about the folder and his files and folders.
     * Al scanned folders will be saved as such object.
     *
     * If a HTTP request ask for a file, than it will be lookup into this type of object.
     * If it exists in this type of object it will maybe get the requested file or {@link network.Response#FILE_NOT_FOUND} error.
     * Depended of the file really exists or not.
     * If not exists in this type of object he will always get a {@link network.Response#FILE_NOT_FOUND} error.
     */
    private class FolderNode {

        /**
         * Generic file of the folder.
         * With type of file is generic is defined in the rule settings file.
         * The name of the generic is always "index", but the extension is various and
         * depended on the definition in the rule settings file.
         */
        private String generic;

        /**
         * Files of the folder.
         */
        private HashMap<String, HttpFile> files;

        /**
         * Folders of the folder.
         */
        private HashMap<String, FolderNode> folders;

        /**
         * Rule settings of the folder.
         */
        private RuleSet ruleSet;

        /**
         * Constructor.
         * @param root Path to the folder.
         */
        FolderNode(String root, RuleSet rules) {
            this(new File(root), root, rules, true);
        }

        /**
         * Constructor.
         * @param folder Folder.
         * @param path Path to the folder.
         * @param root Is the folder the root of the host, defined in the host file.
         * @throws NullPointerException folder == null
         */
        FolderNode(File folder, String path, RuleSet rules, boolean root) throws NullPointerException {
            if (folder == null) {
                throw new NullPointerException("folder == null");
            }

            files = new HashMap<>();
            folders = new HashMap<>();

            // Processing the rule settings file.
            File ruleFile = new File(folder + "/rules.set");
            if (ruleFile.exists()) {
                ruleSet = new RuleSet(ruleFile, rules, root);
            } else {
                ruleSet = rules;
            }

            // Scanning all folders and files and build recursive data structure of files and folders.
            // Exclude all files and folder that must excluded, by following the rules of the rule settings.
            File[] _files = folder.listFiles();
            if (_files != null) {
                for (File entry : _files) {
                    if (!ruleSet.index(entry)) {
                        continue;
                    }

                    if (entry.isDirectory()) {
                        folders.put(entry.getName(), new FolderNode(entry, path + entry.getName() + "/", ruleSet, false));
                    } else if (entry.isFile()) {
                        String extension = getExtension(entry);
                        Mime mime = ruleSet.getMimeType(extension);

                        // if the file's mime type isn't supported or the file is denied from indexing the scanned file will be excluded.
                        if (mime == null || !ruleSet.indexExtension(extension)) {
                            continue;
                        }

                        files.put(entry.getName(), new HttpFile(mime, path + entry.getName()));

                        // if generic, set these file as the generic file of the folder.
                        if (ruleSet.isGenericFile(entry)) {
                            generic = entry.getName();
                        }
                    }
                }
            }
        }

        /**
         * Get a file of the given url.
         * @param url Requested url.
         * @return Requested file.
         */
        HttpFile getFile(Url url) {
            String[] path = url.getPath();
            if (path == null || path[0].equals("")) {
                return url.isFolder() ? files.get(generic) : files.get(url.getFile());
            }

            return getFile(url, path, 0);
        }

        /**
         * Get a file of the given url.
         * @param url Requested url.
         * @param path Path to the requested file.
         * @param index Reading index of the path array.
         * @return Requested file.
         */
        HttpFile getFile(Url url, String[] path, int index) {

            if (path.length <= index) {
                return url.isFolder() ? files.get(generic) : files.get(url.getFile());
            }

            return folders.containsKey(path[index]) ? folders.get(path[index]).getFile(url, path, index + 1) : null;
        }

        /**
         * Has the requested file the correct mime type.
         * @param url Requested url.
         * @return is the requested mime type correct.
         */
        boolean hasSupportedMime(Url url) {
            String[] path = url.getPath();
            if (path == null || path[0].equals("")) {
                return ruleSet.supportMimeType(url.isFolder() ? ruleSet.getGenericExtension() : url.getExtension());
            }

            return hasSupportedMime(url, path, 0);
        }

        /**
         * Has the requested file the correct mime type.
         * @param url Requested url.
         * @param path Path to the requested file.
         * @param index Reading index of the path array.
         * @return is the requested mime type correct.
         */
        boolean hasSupportedMime(Url url, String[] path, int index) {
            return path.length > index && folders.containsKey(path[index]) ?
                    folders.get(path[index]).hasSupportedMime(url, path, index + 1) :
                    ruleSet.supportMimeType(url.isFolder() ? ruleSet.getGenericExtension() : url.getExtension());
        }

        /**
         * Get the rule set of the folder.
         * @return the rule set of the folder.
         */
        RuleSet getRuleSet() {
            return ruleSet;
        }

        /**
         * Get the extension of a file.
         * @param file File to get the extension.
         * @return the extension of a file.
         */
        String getExtension(File file) {
            int i = file.getName().lastIndexOf(".");
            if (i > 0) {
                return file.getName().substring(i + 1);
            }
            return "";
        }
    }

    /**
     * Get the default host, that also is used as fallback if the requested hosts doesn't exists.
     * @return the default host, that also is used as fallback if the requested hosts doesn't exists.
     */
    public String getDefaultHost() {
        return defaultHost;
    }

    /**
     * Virtual file object that holds information about a file.
     */
    public class HttpFile {

        /**
         * Mime type of the file.
         */
        private Mime mime;

        /**
         * Path to the file.
         */
        private String path;

        /**
         * Constructor.
         * @param mime Mime type of the file.
         * @param path Path to the file.
         */
        HttpFile(Mime mime, String path) {
            this.mime = mime;
            this.path = path;
        }

        /**
         * Get the file.
         * @return the file.
         */
        public File getFile() {
            return new File(path);
        }

        /**
         * Get mime type of the file.
         * @return mime type of the file.
         */
        public Mime getMime() {
            return mime;
        }
    }
}
