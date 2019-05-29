/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.oneentity.services.connectedUsers.ConnectedUser;
import com.makesystem.oneentity.services.users.User;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author Richeli.vargas
 */
public class OneUser implements Serializable {

    private final User user;

    private final Collection<ConnectedUser> connections = new LinkedList<>();

    public OneUser(final User user) {
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }

    public Collection<ConnectedUser> getConnections() {
        return connections;
    }

}
