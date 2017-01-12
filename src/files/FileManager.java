package files;

import files.rules.RuleSet;
import network.Mime;
import network.Url;
import network.exception.MimeNotSupported;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManager extends Thread {

    private String defaultHost;
    private HashMap<String, FolderNode> roots;

    public FileManager(String path) {
        roots = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            ArrayList<String> lines = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            for (String l : lines) {
                if (l.startsWith("#")) {
                    continue;
                }

                String[] ls = l.split(" ");
                roots.put(ls[0], new FolderNode(ls[1]));

                if (defaultHost == null) {
                    defaultHost = ls[0];
                }
            }
        } catch (IOException e) {
            System.out.println("error");
        }
    }

    @Override
    public void run() {
        // cache data (later)
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

    private class FolderNode {
        private String generic;
        private String root;

        private HashMap<String, HttpFile> files;
        private HashMap<String, FolderNode> folders;
        private RuleSet ruleSet;

        FolderNode(String root) {
            this(new File(root), root); // "/"
        }

        FolderNode(File folder, String path) throws NullPointerException {
            if (folder == null) {
                throw new NullPointerException("folder == null");
            }

            files = new HashMap<>();
            folders = new HashMap<>();

            File[] _files = folder.listFiles();
            if (_files != null) {
                for (File entry : _files) {
                    if (entry.isDirectory()) {
                        folders.put(entry.getName(), new FolderNode(entry, path + entry.getName() + "/"));
                    } else if (entry.isFile()) {
                        String extension = getExtension(entry);
                        if (extension.equals("set")) {
                            if (entry.getName().equals("rules.set")) {
                                ruleSet = new RuleSet();
                            }
                        } else {
                            Mime mime;
                            try {
                                mime = new Mime(extension);
                            } catch (MimeNotSupported e) {
                                continue;
                            }

                            files.put(entry.getName(), new HttpFile(mime, path + entry.getName()));

                            if (generic == null) {
                                String name = entry.getName();
                                int dot = name.lastIndexOf(".");
                                if (dot != -1 && name.substring(0, dot).equals("index")) {
                                    generic = name;
                                }
                            }
                        }
                    }
                }
            }
        }

        HttpFile getFile(Url url) {
            String[] path = url.getPath();
            if (path == null || path[0].equals("")) {
                return files.get(generic);
            }

            return getFile(url, 0);
        }

        HttpFile getFile(Url url, int index) {
            String[] path = url.getPath();

            if (path.length <= index) {
                return url.isFolder() ? files.get(generic) : files.get(url.getFile());
            }

            return folders.get(path[index]).getFile(url, index + 1);
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
