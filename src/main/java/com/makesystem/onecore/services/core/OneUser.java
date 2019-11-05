/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.oneentity.core.types.ServiceType;
import com.makesystem.oneentity.services.users.storage.UserConnected;
import com.makesystem.oneentity.services.users.storage.User;
import java.io.Serializable;

/**
 *
 * @author Richeli.vargas
 */
public class OneUser implements Serializable {

    private final User user;
    private final UserConnected connection;

    public OneUser(final User user, final UserConnected connection) {
        this.user = user;
        this.connection = connection;
    }

    public User getUser() {
        return user;
    }

    public UserConnected getConnection() {
        return connection;
    }
    
    public ServiceType getService(){
        return connection.getService();
    }
    
    public String getPublicIp(){
        return connection.getPublicIp();
    }
    
    public String getLocalIp(){
        return connection.getLocalIp();
    }
}
