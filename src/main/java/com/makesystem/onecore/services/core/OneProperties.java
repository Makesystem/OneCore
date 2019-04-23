/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.mdbi.core.types.ConnectionType;
import com.makesystem.xeonentity.core.SystemProperty;

/**
 *
 * @author Richeli.vargas
 */
public abstract class OneProperties {

    public static final SystemProperty<String>          DATABASE__HOST      = new SystemProperty("one__db__host",       "127.0.0.1");
    public static final SystemProperty<Integer>         DATABASE__PORT      = new SystemProperty("one__db__port",       27017);
    public static final SystemProperty<String>          DATABASE__NAME      = new SystemProperty("one__db__name",       "one");
    public static final SystemProperty<String>          DATABASE__USER      = new SystemProperty("one__db__user",       "unknow");
    public static final SystemProperty<String>          DATABASE__PASSWORD  = new SystemProperty("one__db__password",   "unknow");
    public static final SystemProperty<ConnectionType>  DATABASE__TYPE      = new SystemProperty("one__db__type",       ConnectionType.MONGO);

    private OneProperties() {        
    }
}
