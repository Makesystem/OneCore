/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.AbstractServerSocket;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Richeli.vargas
 */
@ServerEndpoint("/one_door/{login}/{password}")
public class OneDoorServer extends AbstractServerSocket {

    @Override
    public void onOpen(final Session session) {
        System.out.println("On open >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        sendMessage(session, "on open");
    }

    @Override
    public void onClose(final Session session) throws Throwable {        
    }

    @Override
    public void onMessage(final Session session, final String message) {
        System.out.println("message: " + message);
    }

    @Override
    public void onError(final Throwable throwable, final Session session) {
    }
    
}
