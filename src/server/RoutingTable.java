package server;

import java.util.HashMap;

public class RoutingTable {
    private HashMap<String, String> routes;

    public RoutingTable() {
        routes = new HashMap<>();

        // create routing table out config
    }

    public String getPath(String host) {
        return routes.get(host);
    }
}
