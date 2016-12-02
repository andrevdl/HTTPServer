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

    private HashMap<String, String> routes;
    private HashMap<String, FolderNode> roots;

    public FileManager(String path) {
        routes = new HashMap<>();
        roots = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            ArrayList<String> lines = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            for (String l : lines) {
                String[] ls = l.split(" ");

                routes.put(ls[0], ls[1]);
                roots.put(ls[0], new FolderNode(ls[1]));
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
        return routes.containsKey(host);
    }

    private String getRoot(String host) {
        return routes.get(host);
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
        private boolean top;

        private String generic;
        private String path;

        private HashMap<String, HttpFile> files;
        private HashMap<String, FolderNode> folders;
        private RuleSet ruleSet;

        FolderNode(String root) {
            this(new File(root), "/");
            top = true;
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
                        if (entry.getName().equals("rules.set")) {
                            ruleSet = new RuleSet();
                        } else { // TODO: 2-12-2016 add generic support
                            String extension = "";
                            int i = entry.getName().lastIndexOf(".");
                            if (i > 0) {
                                extension = entry.getName().substring(i+1);
                            }

                            Mime mime;
                            try {
                                mime = new Mime(extension);
                            } catch (MimeNotSupported e) {
                                continue;
                            }

                            files.put(entry.getName(), new HttpFile(mime, path + getName()));
                        }
                    }
                }
            }
        }

        HttpFile getFile(Url url) {
            return null;
        }

        RuleSet getRuleSet() {
            return ruleSet;
        }
    }

    public class HttpFile {
        private Mime mime;
        private String path;

        public HttpFile(Mime mime, String path) {
            this.mime = mime;
            this.path = path;
        }

        public String getData() {
            return null;
        }

        public Mime getMime() {
            return mime;
        }

        public String getPath() {
            return path;
        }
    }
}
