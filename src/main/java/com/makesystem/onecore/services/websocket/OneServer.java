/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.AbstractServerSocket;
import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.mwi.websocket.CloseReason;
import com.makesystem.onecore.services.core.OneProperties;
import com.makesystem.onecore.services.core.OneUser;
import com.makesystem.onecore.services.core.users.ConnectedUserService;
import com.makesystem.onecore.services.core.users.UserActionService;
import com.makesystem.onecore.services.core.users.UserService;
import com.makesystem.oneentity.core.types.Action;
import com.makesystem.oneentity.core.types.MessageType;
import com.makesystem.oneentity.core.types.OneCloseCodes;
import com.makesystem.oneentity.core.websocket.Message;
import com.makesystem.oneentity.services.OneServices.Access;
import com.makesystem.oneentity.services.users.storage.ConnectedUser;
import com.makesystem.oneentity.services.users.storage.User;
import com.makesystem.pidgey.json.ObjectMapperJRE;
import com.makesystem.pidgey.lang.ThrowableHelper;
import com.makesystem.xeoncore.management.Management;
import com.makesystem.xeonentity.core.exceptions.TaggedException;
import com.makesystem.xeonentity.core.types.ServiceType;
import com.makesystem.xeonentity.services.management.storage.LogError;
import javax.websocket.EndpointConfig;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Richeli.vargas
 */
@ServerEndpoint(Access.PATH)
public class OneServer extends AbstractServerSocket<Message> {

    public static interface Tags {

        public static final String ON_STARTUP = "ON_STARTUP";
        public static final String ON_OPEN = "ON_OPEN";
        public static final String ON_CLOSE = "ON_CLOSE";
        public static final String ON_MESSAGE = "ON_MESSAGE";
        public static final String NO_TAGGED_THROWABLE = "NO_TAGGED_THROWABLE";
        public static final String NO_THROWABLE = "NO_THROWABLE";
    }

    private final ConnectedUserService connectedUserService;
    private final UserService userService;
    private final UserActionService userActionService;

    private final Management management;
    private final OneConsumer consumer;

    public OneServer() {
        super();
        connectedUserService = new ConnectedUserService();
        userService = new UserService();
        userActionService = new UserActionService();
        management = Management.getInstance();
        consumer = new OneConsumer();
    }

    @Override
    public int getTimeout() {
        return OneProperties.WEBSOCKET_SERVER__TIMEOUT.getValue();
    }

    @Override
    public void setTimeout(int timeout) {
        OneProperties.WEBSOCKET_SERVER__TIMEOUT.setValue(timeout);
    }

    @Override
    protected final void onStartUp() {
        try {
            connectedUserService.delete(OneProperties.INNER_HTTP__HOST.getValue());
        } catch (final Throwable throwable) {
            onError(null, new TaggedException(Tags.ON_STARTUP, throwable));
        }
    }

    @Override
    protected void onOpen(final SessionData sessionData, final EndpointConfig config) {

        final long startAction = System.currentTimeMillis();

        // Client        
        final String loginOrEmail = sessionData.getParameters().getString(Access.Attributes.LOGIN);
        final String password = sessionData.getParameters().getString(Access.Attributes.PASSWORD);
        final String customer = sessionData.getParameters().getString(Access.Attributes.CUSTOMER);
        final String localIp = sessionData.getParameters().getString(Access.Attributes.LOCAL_IP);
        final String publicIp = sessionData.getParameters().getString(Access.Attributes.PUBLIC_IP);

        //Server
        final String serverName = OneProperties.SERVER_NAME.getValue();
        final String httpHost = OneProperties.INNER_HTTP__HOST.getValue();
        final Integer httpPort = OneProperties.INNER_HTTP__PORT.getValue();

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

                final OneUser oneUser = new OneUser(user, localIp, publicIp);

                // /////////////////////////////////////////////////////////////
                // Create connection register
                // /////////////////////////////////////////////////////////////
                final ConnectedUser connectedUser = new ConnectedUser();
                connectedUser.setUser(user.getId());
                connectedUser.setCustomer(null);
                connectedUser.setPublicIp(publicIp);
                connectedUser.setLocalIp(localIp);
                connectedUser.setServerName(serverName);
                connectedUser.setServerHost(httpHost);
                connectedUser.setServerPort(httpPort.toString());
                connectedUser.setService(ServiceType.ONE);
                connectedUser.setInsertionDate(startAction);

                oneUser.getConnections().add(connectedUserService.insert(connectedUser));

                // /////////////////////////////////////////////////////////////
                // Set session User
                // /////////////////////////////////////////////////////////////
                sessionData.setData(oneUser);

                // /////////////////////////////////////////////////////////////
                // Send the user data to client
                // /////////////////////////////////////////////////////////////
                message.setType(MessageType.RESPONSE_SUCCESS);
                message.setData(ObjectMapperJRE.write(user));
                sessionData.sendObject(message);

                // /////////////////////////////////////////////////////////////
                // Save login action
                // /////////////////////////////////////////////////////////////
                userActionService.insertLoginAction(oneUser, startAction);
            }

        } catch (final Throwable throwable) {
            throw new TaggedException(Tags.ON_OPEN, throwable);
        }
    }

    @Override
    protected void onClose(final SessionData sessionData, final CloseReason closeReason) {
        final OneUser user = sessionData.getData();
        if (user != null) {
            user.getConnections().forEach(connection -> {
                try {
                    connectedUserService.delete(connection);
                } catch (final Throwable throwable) {
                    onError(sessionData, new TaggedException(Tags.ON_CLOSE, throwable));
                }
            });
        }
    }

    @Override
    protected void onMessage(final SessionData sessionData, final Message message) {

        final OneUser oneUser = sessionData.getData();

        try {

            switch (message.getType()) {
                case COMMAND: {

                    // Create a Message to send to the client
                    final Message response = new Message(message.getId());
                    response.setAction(message.getAction());
                    response.setService(message.getService());

                    // Create a Action to register on database
                    try {
                        final String result = userActionService.execute(
                                oneUser.getUser().getId(),
                                oneUser.getLocalIp(),
                                oneUser.getPublicIp(),
                                message.getAction(),
                                () -> consumer.consumer(sessionData, message));
                        //
                        // Call services and get result
                        //
                        response.setType(MessageType.RESPONSE_SUCCESS);
                        response.setData(result);
                    } catch (final Throwable throwable) {
                        // Set message data and status to respose
                        response.setType(MessageType.RESPONSE_ERROR);
                        response.setData(ThrowableHelper.toString(throwable));
                        throw throwable;
                    } finally {
                        // Send response to client
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
            logError.setWhen(System.currentTimeMillis());

            if (sessionData != null) {
                logError.setCustomer(sessionData.getParameters().getString(Access.Attributes.CUSTOMER));

                final OneUser oneUser = (OneUser) sessionData.getData();
                if (oneUser != null
                        && oneUser.getUser() != null
                        && oneUser.getUser().getId() != null) {
                    logError.setUser(oneUser.getUser().getId().getHexString());
                }
            }

            logError.setService(ServiceType.ONE);

            final String message;
            final String stackTrace;

            if (throwable == null) {
                logError.setTag(Tags.NO_THROWABLE);
                message = "No message";
                stackTrace = "No stack trace";
            } else if (throwable instanceof TaggedException) {

                final TaggedException taggedException = (TaggedException) throwable;
                logError.setTag(taggedException.getTag());
                closeConnection = taggedException.getTag().equals(Tags.ON_OPEN);

                final Throwable cause = taggedException.getCause();
                if (cause == null) {
                    stackTrace = ThrowableHelper.toString(throwable);
                } else {
                    stackTrace = ThrowableHelper.toString(cause);
                }

                message = stackTrace == null || stackTrace.isEmpty()
                        ? "No message"
                        : stackTrace.split("\n")[0];

            } else {
                logError.setTag(Tags.NO_TAGGED_THROWABLE);
                stackTrace = ThrowableHelper.toString(throwable);
                message = stackTrace == null || stackTrace.isEmpty()
                        ? "No message"
                        : stackTrace.split("\n")[0];
            }

            logError.setMessage(message);
            logError.setStackTrace(stackTrace);

            if (management != null) {
                management.saveLog(logError);
            }

        } catch (final Throwable ignore) {
            ignore.printStackTrace();
        } finally {
            if (closeConnection && sessionData != null) {
                sessionData.close();
            }
        }
    }

    @Override
    protected Message decodeMessage(final String string) throws Throwable {
        return ObjectMapperJRE.read(string, Message.class);
    }

    @Override
    protected String encodeMessage(final Message message) throws Throwable {
        return ObjectMapperJRE.write(message);
    }

}
