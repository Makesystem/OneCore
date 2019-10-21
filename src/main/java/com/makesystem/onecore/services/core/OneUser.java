/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.oneentity.services.users.storage.UserConnected;
import com.makesystem.oneentity.services.users.storage.User;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Richeli.vargas
 */
public class OneUser implements Serializable {

    private final User user;
    private final String localIp;
    private final String publicIp;

    private final Collection<UserConnected> connections = new ConcurrentLinkedQueue<>();

    public OneUser(User user, String localIp, String publicIp) {
        this.user = user;
        this.localIp = localIp;
        this.publicIp = publicIp;
    }

    public User getUser() {
        return user;
    }

    public Collection<UserConnected> getConnections() {
        return connections;
    }

    public UserConnected getConnection(final String sessionId) {
        if (sessionId == null) {
            return null;
        }
        return connections.stream()
                .filter(connection -> Objects.equals(connection.getSessionId(), sessionId))
                .findAny().orElse(null);
    }

    public boolean remove(final UserConnected connection) {
        return connections.remove(connection);
    }

    public String getLocalIp() {
        return localIp;
    }

    public String getPublicIp() {
        return publicIp;
    }
}
