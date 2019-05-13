/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.oneentity.core.websocket.Message;

/**
 *
 * @author Richeli.vargas
 */
public class OneConsumer {
    
    public <D> String consumer(final SessionData sessionData, final Message message){
        switch(message.getAction()){
            case ONE__ECHO:
                return echo(message.getData());
            default:
                throw new IllegalArgumentException("Unknow action: " + message.getAction());
        }
    }
    
    protected <D> String echo(final D data){
        return String.valueOf(data);
    }
}
