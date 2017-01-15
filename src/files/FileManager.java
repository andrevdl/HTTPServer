package files;

import network.Mime;
import files.rules.RuleSet;
import network.Url;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class FileManager {

    private String defaultHost;
    private HashMap<String, FolderNode> roots;
    private RuleSet baseRuleSet;

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

    public boolean hostExists(String host) {
        return roots.containsKey(host);
    }

    public synchronized HttpFile getFile(String host, Url url) {
        FolderNode node = roots.get(host);
        return node != null ? node.getFile(url) : null;
    }

    public synchronized RuleSet getRootRuleSet(String host) {
        FolderNode node = roots.get(host);
        return node != null ? node.getRuleSet() : null;
    }

    private class HostListener implements ConfigReaderListener {

        @Override
        public boolean allowedProperty(String name, int args) {
            return args == 2;
        }

        @Override
        public boolean onReadProperty(String[] args) {
            roots.put(args[0], new FolderNode(args[1]));

            if (defaultHost == null) {
                defaultHost = args[0];
            }

            return true;
        }
    }

    private class FolderNode {
        private String generic;

        private HashMap<String, HttpFile> files;
        private HashMap<String, FolderNode> folders;
        private RuleSet ruleSet;

        FolderNode(String root) {
            this(new File(root), root, true);
        }

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

        HttpFile getFile(Url url) {
            String[] path = url.getPath();
            if (path == null || path[0].equals("")) {
                return url.isFolder() ? files.get(generic) : files.get(url.getFile());
            }

            return getFile(url, 0);
        }

        HttpFile getFile(Url url, int index) {
            String[] path = url.getPath();

            if (path.length <= index) {
                return url.isFolder() ? files.get(generic) : files.get(url.getFile());
            }

            return folders.containsKey(path[index]) ? folders.get(path[index]).getFile(url, index + 1) : null;
        }

        RuleSet getRuleSet() {
            return ruleSet;
        }

        String getExtension(File file) {
            int i = file.getName().lastIndexOf(".");
            if (i > 0) {
                return file.getName().substring(i + 1);
            }
            return "";
        }
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    public class HttpFile {
        private Mime mime;
        private String path;

        public HttpFile(Mime mime, String path) {
            this.mime = mime;
            this.path = path;
        }

        public File getFile() {
            return new File(path);
        }

        public Mime getMime() {
            return mime;
        }
    }
}
