/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.mwi.websocket.CloseReason;
import com.makesystem.onecore.services.core.OneProperties;
import com.makesystem.onecore.services.core.OneUser;
import com.makesystem.onecore.services.core.users.UserConnectedService;
import com.makesystem.onecore.services.core.users.UserActionService;
import com.makesystem.onecore.services.core.users.UserService;
import com.makesystem.oneentity.core.types.Action;
import com.makesystem.oneentity.core.types.OneCloseCodes;
import com.makesystem.oneentity.core.types.ServiceType;
import com.makesystem.oneentity.services.OneServices.Access;
import com.makesystem.oneentity.services.users.storage.UserConnected;
import com.makesystem.oneentity.services.users.storage.User;
import com.makesystem.pidgey.interfaces.AsyncCallback;
import com.makesystem.pidgey.json.ObjectMapperJRE;
import com.makesystem.pidgey.lang.ThrowableHelper;
import com.makesystem.xeoncore.core.AbstractServerSocket;
import com.makesystem.xeonentity.core.exceptions.TaggedException;
import com.makesystem.xeonentity.core.types.MessageType;
import com.makesystem.xeonentity.core.websocket.Message;
import com.makesystem.xeonentity.services.management.storage.LogError;
import com.mongodb.MongoClientException;
import com.mongodb.MongoTimeoutException;
import java.sql.SQLException;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Richeli.vargas
 */
@ServerEndpoint(Access.PATH)
public class OneServer extends AbstractServerSocket {

    public static interface Tags {

        public static final String ON_STARTUP = "ON_STARTUP";
        public static final String ON_OPEN = "ON_OPEN";
        public static final String ON_CLOSE = "ON_CLOSE";
        public static final String ON_MESSAGE = "ON_MESSAGE";
        public static final String NO_TAGGED_THROWABLE = "NO_TAGGED_THROWABLE";
        public static final String NO_THROWABLE = "NO_THROWABLE";
    }

    private final UserConnectedService connectedUserService;
    private final UserService userService;
    private final UserActionService userActionService;

    private final OneConsumer consumer;

    public OneServer() {
        super(2, 2, 20, 2);
        connectedUserService = new UserConnectedService();
        userService = new UserService();
        userActionService = new UserActionService();
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
    protected String getInnerHost() {
        return OneProperties.INNER_HTTP__HOST.getValue();
    }

    @Override
    protected Integer getInnerPort() {
        return OneProperties.INNER_HTTP__PORT.getValue();
    }

    @Override
    protected String getClientHost(final Session session) {
        return session.getPathParameters().get(Access.Attributes.PUBLIC_IP);
    }

    @Override
    protected Integer getClientPort(final Session session) {
        return null;
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
            message.setService(ServiceType.ONE.toString());
            message.setAction(Action.ONE__LOGIN.toString());

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
                final UserConnected connectedUser = new UserConnected();
                connectedUser.setSessionId(sessionData.getSession().getId());
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

            final int reasonCode = getCloseReasonFor(throwable);
            final String reasonPhrase = throwable.getMessage();
            final CloseReason reason = buildReason(reasonCode, reasonPhrase);
            sessionData.close(reason);

            throw new TaggedException(Tags.ON_OPEN, throwable);
        }
    }

    @Override
    protected void onClose(final SessionData sessionData, final CloseReason closeReason) {

        final OneUser user = sessionData.getData();
        if (user != null) {

            final long startAction = System.currentTimeMillis();

            // /////////////////////////////////////////////////////////////////
            // Remove connection register
            // /////////////////////////////////////////////////////////////////
            final UserConnected connection = user.getConnection(sessionData.getSession().getId());
            if (connection != null) {
                try {
                    connectedUserService.delete(connection);
                } catch (final Throwable throwable) {
                    onError(sessionData, new TaggedException(Tags.ON_CLOSE, throwable));
                } finally {
                    user.remove(connection);
                }
            }

            try {
                // /////////////////////////////////////////////////////////////
                // Save login action
                // /////////////////////////////////////////////////////////////
                userActionService.insertLoginAction(user, startAction);
            } catch (final Throwable throwable) {
                onError(sessionData, new TaggedException(Tags.ON_CLOSE, throwable));
            }
        }
    }

    @Override
    protected void onMessage(final SessionData sessionData, final Message message) {

        final OneUser oneUser = sessionData.getData();

        try {

            switch (message.getType()) {
                case COMMAND: {

                    // Create a Action to register on database
                    final AsyncCallback<String> callback = new AsyncCallback<String>() {
                        
                        @Override
                        public void onSuccess(final String response) {
                            sendResponse(MessageType.RESPONSE_SUCCESS, response);
                        }

                        @Override
                        public void onFailure(final Throwable throwable) {
                            sendResponse(MessageType.RESPONSE_ERROR, ThrowableHelper.toString(throwable));
                            onError(sessionData, new TaggedException(Tags.ON_MESSAGE, throwable));
                        }

                        void sendResponse(final MessageType messageType, final String data) {
                            // Create a Message to send to the client
                            final Message response = new Message(message.getId());
                            response.setAction(message.getAction());
                            response.setService(message.getService());
                            response.setType(messageType);
                            response.setData(data);
                            sessionData.sendObject(response);
                        }
                    };

                    userActionService.execute(
                            oneUser.getUser().getId(),
                            oneUser.getLocalIp(),
                            oneUser.getPublicIp(),
                            Action.valueOf(message.getAction()),
                            () -> consumer.consumer(sessionData, message, callback));

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

    protected int getCloseReasonFor(final Throwable throwable) {

        if (throwable == null) {
            return OneCloseCodes.UNKNOW_ERROR.getCode();
        }

        if (throwable instanceof MongoTimeoutException) {
            return OneCloseCodes.DATABASE_IS_NOT_ACCESSIBLE.getCode();
        } else if (throwable instanceof MongoClientException) {
            return OneCloseCodes.UNKNOW_DATABASE_ERROR.getCode();
        } else if (throwable instanceof SQLException) {
            return OneCloseCodes.UNKNOW_DATABASE_ERROR.getCode();
        } else {
            return getCloseReasonFor(throwable.getCause());
        }

    }
}
