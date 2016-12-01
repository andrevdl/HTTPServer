package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by André on 30-11-2016.
 */
public class FileManager {
    private HashMap<String, String> routes;

    public FileManager(String path) {
        routes = new HashMap<>();

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
            }
        } catch (IOException e) {
            System.out.println("error");
        }
    }

    public String getPath(String host) {
        return routes.get(host);
    }
}
