/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.AbstractServerSocket;
import com.makesystem.mwc.websocket.server.OnCloseHandler;
import com.makesystem.mwc.websocket.server.OnErrorHandler;
import com.makesystem.mwc.websocket.server.OnMessageHandler;
import com.makesystem.mwc.websocket.server.OnOpenHandler;
import com.makesystem.mwc.websocket.server.OnOpenParameter;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Richeli.vargas
 */
@ServerEndpoint("/one_door/{login}/{password}")
public class OneDoorServer extends AbstractServerSocket {

    @OnOpenHandler
    public void onOpen(final Session session, 
            @OnOpenParameter("login") final String login, 
            @OnOpenParameter("password") final String password) {
        System.out.println("On open >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        sendMessage(session, "on open");
    }

    @OnCloseHandler
    public void onClose(final Session session) throws Throwable {        
    }

    @OnMessageHandler
    public void onMessage(final Session session, final String message) {
        System.out.println("message: " + message);
    }

    @OnErrorHandler
    public void onError(final Session session, final Throwable throwable) {
    }
    
}
