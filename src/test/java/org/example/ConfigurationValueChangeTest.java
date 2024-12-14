package org.example;

import org.apache.zookeeper.*;

import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public class ConfigurationValueChangeTest {

    private static final String ZK_SERVER = "localhost:2181"; // ZooKeeper server address
    private static final int SESSION_TIMEOUT = 5000;

    @Test
    public void testConfigurationCreatedWithNoWatch() {
        try {
            // Connect to ZooKeeper
            ZooKeeper zooKeeper = new ZooKeeper(ZK_SERVER, SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("Connected to ZooKeeper");
                }
            });

            String dbURL = "jdbc:mysql://localhost:3306/mydb";
            String username = "admin";
            String password = "password123";

            // Create the configuration hierarchy
            createNodeIfNotExists(zooKeeper, "/app/config/db.url", dbURL);
            createNodeIfNotExists(zooKeeper, "/app/config/db.username", username);
            createNodeIfNotExists(zooKeeper, "/app/config/db.password", password);

            String dbUrl = new String(zooKeeper.getData("/app/config/db.url", false, null));
            String dbUsername = new String(zooKeeper.getData("/app/config/db.username", false, null));
            String dbPassword = new String(zooKeeper.getData("/app/config/db.password", false, null));

            Assertions.assertEquals(dbURL, dbUrl);
            Assertions.assertEquals(username, dbUsername);
            Assertions.assertEquals(password, dbPassword);

            deleteNodeIfExists(zooKeeper,"/app/config/db.url");
            deleteNodeIfExists(zooKeeper,"/app/config/db.username");
            deleteNodeIfExists(zooKeeper,"/app/config/db.password");

            // Close the ZooKeeper connection
            zooKeeper.close();
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void testWatchedNodeAndReceiveEventSuccessfully() {
        try {
            // Connect to ZooKeeper
            ZooKeeper zooKeeper = new ZooKeeper(ZK_SERVER, SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("Connected to ZooKeeper");
                }
            });

            String dbURL = "jdbc:mysql://localhost:3306/mydb";
            String appConfigPath = "/app/config/db.url";

            // Create the configuration hierarchy
            createNodeIfNotExists(zooKeeper, appConfigPath, dbURL);

            String dbUrl = new String(zooKeeper.getData(appConfigPath, false, null));

            Assertions.assertEquals(dbURL, dbUrl);

            CompletableFuture<String> changedUrlFuture = new CompletableFuture<>();

            Stat stat = zooKeeper.exists(appConfigPath, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
                    try {
                        // Fetch and print the updated value
                        String newValue = new String(zooKeeper.getData(appConfigPath, false, null));
                        changedUrlFuture.complete(newValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            var changedUrl = "jdbc:mysql://localhost:3306/mydb2";
            zooKeeper.setData(appConfigPath, changedUrl.getBytes(), -1);

            Assertions.assertEquals(changedUrl, changedUrlFuture.get());

            // clean up
            deleteNodeIfExists(zooKeeper,"/app/config/db.url");

            // Close the ZooKeeper connection
            zooKeeper.close();
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    private static void watchNode(ZooKeeper zooKeeper, String path) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, event -> {
            if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
                System.out.println("Node data changed: " + event.getPath());
                try {
                    // Re-register the watch to continue receiving updates
                    watchNode(zooKeeper, path);

                    // Fetch and print the updated value
                    String newValue = new String(zooKeeper.getData(path, false, null));
                    System.out.println("New value: " + newValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (stat != null) {
            System.out.println("Watch established on node: " + path);
        } else {
            System.out.println("Node does not exist: " + path);
        }
    }

    private static void deleteNodeIfExists(ZooKeeper zooKeeper, String path) throws KeeperException, InterruptedException {
        // Check if the node exists
        if (zooKeeper.exists(path, false) != null) {
            // Get the list of child nodes
            for (String child : zooKeeper.getChildren(path, false)) {
                // Recursively delete child nodes
                deleteNodeIfExists(zooKeeper, path + "/" + child);
            }
            // Delete the node itself
            zooKeeper.delete(path, -1); // Use -1 to ignore version
            System.out.println("Deleted node: " + path);
        } else {
            System.out.println("Node does not exist: " + path);
        }
    }

    private static void createNodeIfNotExists(ZooKeeper zooKeeper, String path, String data) throws KeeperException, InterruptedException {
        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (int i = 1; i < parts.length; i++) {
            currentPath.append("/").append(parts[i]);
            if (zooKeeper.exists(currentPath.toString(), false) == null) {
                zooKeeper.create(
                        currentPath.toString(),
                        (i == parts.length - 1) ? data.getBytes() : new byte[0], // Add data only for the last node
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT
                );
                System.out.println("Created node: " + currentPath + " with data: " + (i == parts.length - 1 ? data : ""));
            }
        }
    }
}
