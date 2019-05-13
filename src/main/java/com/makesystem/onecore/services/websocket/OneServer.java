/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.utils.DebugHelper;
import com.makesystem.mwc.websocket.server.AbstractServerSocket;
import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.mwi.websocket.CloseReason;
import com.makesystem.onecore.services.core.users.UserService;
import com.makesystem.oneentity.core.types.Action;
import com.makesystem.oneentity.core.types.MessageType;
import com.makesystem.oneentity.core.types.OneCloseCodes;
import com.makesystem.oneentity.core.websocket.Message;
import com.makesystem.oneentity.services.users.User;
import com.makesystem.pidgey.json.JsonConverter;
import com.makesystem.pidgey.lang.ThrowableHelper;
import com.makesystem.xeoncore.services.management.crudLogErrorService.CrudLogErrorService;
import com.makesystem.xeonentity.core.exceptions.TaggedException;
import com.makesystem.xeonentity.core.types.ServiceType;
import com.makesystem.xeonentity.services.management.LogError;
import javax.websocket.EndpointConfig;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Richeli.vargas
 */
@ServerEndpoint(OneServer.PATH)
public class OneServer extends AbstractServerSocket<Message> {

    public static final String CONTEXT = "access";
    public static final String PATH = "/"
            + CONTEXT
            + "/{"
            + Params.LOGIN
            + "}/{"
            + Params.PASSWORD
            + "}";

    public static interface Params {

        public static final String LOGIN = "login";
        public static final String PASSWORD = "password";
    }

    public static interface Tags {

        public static final String ON_OPEN = "ON_OPEN";
        public static final String ON_CLOSE = "ON_CLOSE";
        public static final String ON_MESSAGE = "ON_MESSAGE";
        public static final String NO_TAGGED_THROWABLE = "NO_TAGGED_THROWABLE";
        public static final String NO_THROWABLE = "NO_THROWABLE";
    }

    private final CrudLogErrorService errorService = new CrudLogErrorService();
    private final UserService userService = new UserService();

    @Override
    protected void onOpen(final SessionData sessionData, final EndpointConfig config) {

        DebugHelper.printSessionData(sessionData.getSession());

        final String loginOrEmail = sessionData.getParameters().getString(Params.LOGIN);
        final String password = sessionData.getParameters().getString(Params.PASSWORD);

        try {

            // Find user by ((login or e-mail) and password)
            final User user = userService.find(loginOrEmail, password);

            // Create a message to send for the client
            final Message message = new Message();
            message.setService(ServiceType.ONE);
            message.setAction(Action.ONE__LOGIN);

            if (user == null) {

                // 
                final int code = OneCloseCodes.LOGIN_OR_PASSWORD_IS_INVALID.getCode();
                final CloseReason closeReason = buildReason(code, "Login or e-mail is wrong");

                // User not found
                sessionData.close(closeReason);

            } else {

                // Set session User
                sessionData.setData(user);

                // Send the user data to client
                message.setType(MessageType.RESPONSE_SUCCESS);
                message.setData(JsonConverter.write(user));
                sessionData.sendObject(message);

            }

        } catch (final Throwable throwable) {
            throw new TaggedException(Tags.ON_OPEN, throwable);
        }
    }

    @Override
    protected void onClose(final SessionData sessionData, final CloseReason closeReason) {
        System.out.println("OnClose: " + closeReason.getCloseCode() + "|" + closeReason.getCloseCode().getCode() + "|" + closeReason.getReasonPhrase());
    }

    @Override
    protected void onMessage(final SessionData sessionData, final Message message) {
        
        try {

            switch (message.getType()) {
                case COMMAND: {
                    
                    final Message response = new Message(message.getId());
                    response.setAction(message.getAction());
                    response.setService(message.getService());

                    try {
                        //
                        // Call services and get result
                        //
                        response.setType(MessageType.RESPONSE_SUCCESS);
                        response.setData("Success");
                    } catch (final Throwable throwable) {
                        response.setType(MessageType.RESPONSE_ERROR);                        
                        response.setData(ThrowableHelper.toString(throwable));
                        throw throwable;
                    } finally {
                        sessionData.sendObject(response);
                    }
                    
                }
                break;
                case RESPONSE_SUCCESS:
                    break;
                case RESPONSE_ERROR:
                    break;
            }

        } catch (final Throwable throwable) {
            throw new TaggedException(Tags.ON_MESSAGE, throwable);
        }
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    protected void onError(final SessionData sessionData, final Throwable throwable) {

        boolean closeConnection = false;

        try {

            final LogError logError = new LogError();

            logError.setCustomer(sessionData.getParameters().getString("document"));
            logError.setService(ServiceType.ONE);

            if (throwable == null) {
                logError.setTag(Tags.NO_THROWABLE);
                logError.setMessage("No message");
                logError.setStackTrace("No stack trace");
            } else if (throwable instanceof TaggedException) {

                final TaggedException taggedException = (TaggedException) throwable;
                logError.setTag(taggedException.getTag());
                closeConnection = taggedException.getTag().equals(Tags.ON_OPEN);

                final Throwable cause = taggedException.getCause();
                if (cause == null) {
                    logError.setMessage(throwable.getMessage());
                    logError.setStackTrace(ThrowableHelper.toString(throwable));
                } else {
                    logError.setMessage(cause.getMessage());
                    logError.setStackTrace(ThrowableHelper.toString(cause));
                }

            } else {
                logError.setTag(Tags.NO_TAGGED_THROWABLE);
                logError.setMessage(throwable.getMessage());
                logError.setStackTrace(ThrowableHelper.toString(throwable));
            }

            errorService.insert(logError);

        } catch (final Throwable ignore) {
            ignore.printStackTrace();
        } finally {
            if (closeConnection) {
                sessionData.close();
            }
        }
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
