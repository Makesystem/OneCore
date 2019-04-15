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
import java.util.stream.Collectors;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Richeli.vargas
 */
@ServerEndpoint("/one_door/{login}/{password}/{some_int}")
public class OneDoorServer extends AbstractServerSocket {

    @OnOpenHandler
    public void onOpen(final Session session,
            @OnOpenParameter("login") final String login,
            @OnOpenParameter("password") final String password,
            @OnOpenParameter("some_int") final Integer someInt) {
        
        final String userInfo = session.getUserProperties().entrySet().stream().map(entry -> "[" + entry.getKey() + ":" + entry.getValue() + "]").collect(Collectors.joining(" "));
        
        final String echo = "login: "
                + login
                + ":"
                + password
                + ":"
                + someInt
                + ":user_info:"
                + userInfo;
        System.out.println(echo);
        sendMessage(session, echo);

    }

    @OnCloseHandler
    public void onClose(final Session session) throws Throwable {
    }

    @OnMessageHandler
    public void onMessage(final Session session, final String message) {
        System.out.println("message: " + message);
        sendMessage(session, message);
    }

    @OnErrorHandler
    public void onError(final Session session, final Throwable throwable) {
    }

}
