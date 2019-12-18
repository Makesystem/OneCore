/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mdbi.nosql.SimpleObjectId;
import com.makesystem.mwc.websocket.server.DefaultEndpointConfig;

import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.mwi.exceptions.RequestException;
import com.makesystem.mwi.websocket.CloseReason;
import com.makesystem.onecore.services.core.OneProperties;
import com.makesystem.onecore.services.core.OneUser;
import com.makesystem.onecore.services.core.access.LoginCtrl;
import com.makesystem.onecore.services.core.users.UserActionService;
import com.makesystem.oneentity.core.types.Action;
import com.makesystem.oneentity.core.types.OneCloseCodes;
import com.makesystem.oneentity.core.types.ServiceType;
import com.makesystem.oneentity.services.OneServices.Access;
import com.makesystem.oneentity.services.users.storage.UserConnected;
import com.makesystem.pidgey.interfaces.AsyncCallback;
import com.makesystem.pidgey.json.ObjectMapperJRE;
import com.makesystem.pidgey.lang.ThrowableHelper;
import com.makesystem.xeoncore.core.AbstractServerSocket;
import com.makesystem.xeoncore.core.BasicRequestIdentification;
import com.makesystem.xeonentity.core.exceptions.TaggedException;
import com.makesystem.xeonentity.core.types.MessageType;
import com.makesystem.xeonentity.core.websocket.Message;
import com.makesystem.xeonentity.services.management.storage.LogError;
import com.mongodb.MongoClientException;
import com.mongodb.MongoTimeoutException;
import java.sql.SQLException;
import javax.websocket.EndpointConfig;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Richeli.vargas
 */
@ServerEndpoint(value = Access.PATH, configurator = DefaultEndpointConfig.class)
public class OneServer extends AbstractServerSocket {

    private static final long serialVersionUID = 3022211504301278241L;

    public static interface Tags {

        public static final String ON_STARTUP = "ON_STARTUP";
        public static final String ON_OPEN = "ON_OPEN";
        public static final String ON_CLOSE = "ON_CLOSE";
        public static final String ON_MESSAGE = "ON_MESSAGE";
        public static final String NO_TAGGED_THROWABLE = "NO_TAGGED_THROWABLE";
        public static final String NO_THROWABLE = "NO_THROWABLE";
    }

    private final LoginCtrl loginCtrl;
    private final UserActionService userActionService;

    private final OneConsumer consumer;

    public OneServer() {
        super(2, 2, 20, 2);
        loginCtrl = LoginCtrl.getInstance();
        userActionService = UserActionService.getInstance();
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
            loginCtrl.doStartUp();
        } catch (final Throwable throwable) {
            onError(null, new TaggedException(Tags.ON_STARTUP, throwable));
        }
    }

    @Override
    protected void onOpen(final SessionData sessionData, final EndpointConfig config) {

        try {

            // /////////////////////////////////////////////////////////////////
            // Call login rules
            // /////////////////////////////////////////////////////////////////
            final OneUser oneUser = loginCtrl.doLogin(sessionData);

            // /////////////////////////////////////////////////////////////////
            // Send the user data to client
            // /////////////////////////////////////////////////////////////////
            final Message message = new Message();
            message.setAction(Action.ONE__LOGIN.toString());
            message.setType(MessageType.RESPONSE_SUCCESS);
            message.setData(ObjectMapperJRE.write(oneUser.getUser()));

            sessionData.sendObject(message);

        } catch (final Throwable throwable) {

            final int reasonCode = getCloseReasonFor(throwable);
            final String reasonPhrase = throwable.getMessage();
            final CloseReason reason = buildReason(reasonCode, reasonPhrase);
            sessionData.close(reason);

            if (!(throwable instanceof RequestException)) {
                throw new TaggedException(Tags.ON_OPEN, throwable);
            }

        }
    }

    @Override
    protected void onClose(final SessionData sessionData, final CloseReason closeReason) {
        try {
            loginCtrl.doLogoff(sessionData, closeReason);
        } catch (final Throwable throwable) {
            onError(sessionData, new TaggedException(Tags.ON_CLOSE, throwable));
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
                //logError.setCustomer(sessionData.getParameters().getString(Access.Attributes.CUSTOMER));

                final OneUser oneUser = (OneUser) sessionData.getData();
                if (oneUser != null
                        && oneUser.getUser() != null
                        && oneUser.getUser().getId() != null) {
                    logError.setUser(oneUser.getUser().getId().getHexString());
                }
            }

            logError.setService(ServiceType.ONE.toString());

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
        return ObjectMapperJRE.read(string, Message.class
        );
    }

    @Override
    protected String encodeMessage(final Message message) throws Throwable {
        return ObjectMapperJRE.write(message);
    }

    protected int getCloseReasonFor(final Throwable throwable) {

        if (throwable == null) {
            return OneCloseCodes.UNKNOW_ERROR.getCode();
        }

        if (throwable instanceof RequestException) {
            return ((RequestException) throwable).getStatusCode();
        } else if (throwable instanceof MongoTimeoutException) {
            return OneCloseCodes.DATABASE_IS_NOT_ACCESSIBLE.getCode();
        } else if (throwable instanceof MongoClientException) {
            return OneCloseCodes.UNKNOW_DATABASE_ERROR.getCode();
        } else if (throwable instanceof SQLException) {
            return OneCloseCodes.UNKNOW_DATABASE_ERROR.getCode();
        } else {
            return getCloseReasonFor(throwable.getCause());
        }

    }
    
    @Override
    public BasicRequestIdentification getRequestIdentification(final SessionData sessionData) {
        
        final OneUser oneUser = sessionData.getData();
        final UserConnected connection = oneUser.getConnection();
        
        final SimpleObjectId customerId = connection == null ? null : connection.getCustomer();        
        final String customer = customerId == null ? null : customerId.getHexString();
        
        final SimpleObjectId userId = connection == null ? null : connection.getUser();
        final String user = userId == null ? null : userId.getHexString();
        
        final String apiKey = sessionData.getRequestData().getApiKey();
        final ServiceType service = oneUser.getService();
        
        final BasicRequestIdentification identification = new BasicRequestIdentification();
        identification.setCustomer(customer);
        identification.setUser(user);
        identification.setApiKey(apiKey);
        identification.setServiceType(service);
        
        return identification;
    }
}
