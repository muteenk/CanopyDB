package org.canopydb.models;

import java.util.UUID;

public class ConnectionMeta {
    private final String id;
    private String name;
    private String host;
    private int port;
    private String username;
    private String password;
    private ConnectionLabel label;

    // Private Default Constructor for Jackson to serialize this class
    private ConnectionMeta() {
        this.id = UUID.randomUUID().toString(); // Fallback ID if not in JSON
    }

    public ConnectionMeta(
            String name,
            String host,
            int port,
            String username,
            String password,
            ConnectionLabel label
    ) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.label = label;
    }

    public ConnectionMeta(
            String id,
            String name,
            String host,
            int port,
            String username,
            String password,
            ConnectionLabel label
    ) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnectionLabel getLabel() {
        return label;
    }

    public void setLabel(ConnectionLabel label) {
        this.label = label;
    }
}
