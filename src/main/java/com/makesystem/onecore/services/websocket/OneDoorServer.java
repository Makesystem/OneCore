/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.AbstractServerSocket;
import com.makesystem.pidgey.json.JsonConverter;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Richeli.vargas
 */
@ServerEndpoint("/one_door/{login}/{password}/{some_int}")
public class OneDoorServer extends AbstractServerSocket<Message> {

    @Override
    protected void onOpen(final Session session, final EndpointConfig config) {
        System.out.println("OnOpen");
    }

    @Override
    protected void onClose(Session session, CloseReason closeReason) {
        System.out.println("OnClose");
    }

    @Override
    protected void onMessage(final Session session, final Message message) {
        System.out.println("OnMessage: " + message.getData());
        sendMessage(session, message);
    }

    @Override
    protected void onError(final Session session, final Throwable throwable) {
        System.out.println("OnError: " + throwable.getMessage());
    }

    @Override
    protected Message decodeMessage(final String string) throws Throwable {
        return JsonConverter.read(string, Message.class);
    }

    @Override
    protected String encodeMessage(final Message message) throws Throwable {
        return JsonConverter.write(message);
    }
 
}
