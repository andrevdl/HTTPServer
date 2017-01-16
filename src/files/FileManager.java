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
     *
     */
    private String defaultHost;

    /**
     *
     */
    private HashMap<String, FolderNode> roots;

    /**
     *
     */
    private RuleSet baseRuleSet;

    /**
     *
     * @param path
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
     *
     * @param host
     * @return
     */
    public boolean hostExists(String host) {
        return roots.containsKey(host);
    }

    /**
     *
     * @param host
     * @param url
     * @return
     */
    public synchronized HttpFile getFile(String host, Url url) {
        FolderNode node = roots.get(host);
        return node != null ? node.getFile(url) : null;
    }

    /**
     *
     * @param host
     * @return
     */
    public synchronized RuleSet getRootRuleSet(String host) {
        FolderNode node = roots.get(host);
        return node != null ? node.getRuleSet() : null;
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
            roots.put(args[0], new FolderNode(args[1]));

            if (defaultHost == null) {
                defaultHost = args[0];
            }

            return true;
        }
    }

    /**
     *
     */
    private class FolderNode {

        /**
         *
         */
        private String generic;

        /**
         *
         */
        private HashMap<String, HttpFile> files;

        /**
         *
         */
        private HashMap<String, FolderNode> folders;

        /**
         *
         */
        private RuleSet ruleSet;

        /**
         *
         * @param root
         */
        FolderNode(String root) {
            this(new File(root), root, true);
        }

        /**
         *
         * @param folder
         * @param path
         * @param root
         * @throws NullPointerException
         */
        FolderNode(File folder, String path, boolean root) throws NullPointerException {
            if (folder == null) {
                throw new NullPointerException("folder == null");
            }

            files = new HashMap<>();
            folders = new HashMap<>();

            File ruleFile = new File(folder + "/rules.set");
            if (ruleFile.exists()) {
                ruleSet = new RuleSet(ruleFile, baseRuleSet, root);
            } else {
                ruleSet = baseRuleSet;
            }

            File[] _files = folder.listFiles();
            if (_files != null) {
                for (File entry : _files) {
                    if (!ruleSet.index(entry)) {
                        continue;
                    }

                    if (entry.isDirectory()) {
                        folders.put(entry.getName(), new FolderNode(entry, path + entry.getName() + "/", false));
                    } else if (entry.isFile()) {
                        String extension = getExtension(entry);
                        Mime mime = ruleSet.getMimeType(extension);
                        if (mime == null) {
                            continue;
                        }

                        files.put(entry.getName(), new HttpFile(mime, path + entry.getName()));

                        if (ruleSet.isGenericFile(entry)) {
                            generic = entry.getName();
                        }
                    }
                }
            }
        }

        /**
         *
         * @param url
         * @return
         */
        HttpFile getFile(Url url) {
            String[] path = url.getPath();
            if (path == null || path[0].equals("")) {
                return url.isFolder() ? files.get(generic) : files.get(url.getFile());
            }

            return getFile(url, 0);
        }

        /**
         *
         * @param url
         * @param index
         * @return
         */
        HttpFile getFile(Url url, int index) {
            String[] path = url.getPath();

            if (path.length <= index) {
                return url.isFolder() ? files.get(generic) : files.get(url.getFile());
            }

            return folders.containsKey(path[index]) ? folders.get(path[index]).getFile(url, index + 1) : null;
        }

        /**
         *
         * @return
         */
        RuleSet getRuleSet() {
            return ruleSet;
        }

        /**
         *
         * @param file
         * @return
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
     *
     * @return
     */
    public String getDefaultHost() {
        return defaultHost;
    }

    /**
     *
     */
    public class HttpFile {

        /**
         *
         */
        private Mime mime;

        /**
         *
         */
        private String path;

        /**
         *
         * @param mime
         * @param path
         */
        public HttpFile(Mime mime, String path) {
            this.mime = mime;
            this.path = path;
        }

        /**
         *
         * @return
         */
        public File getFile() {
            return new File(path);
        }

        /**
         *
         * @return
         */
        public Mime getMime() {
            return mime;
        }
    }
}
