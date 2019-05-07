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
import com.makesystem.xeonentity.core.types.UserType;
import com.makesystem.xeonentity.services.management.LogError;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.websocket.EndpointConfig;
import javax.websocket.server.ServerEndpoint;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Richeli.vargas
 */
@ServerEndpoint(OneServer.PATH)
public class OneServer extends AbstractServerSocket<Message> {

    public static final String CONTEXT = "one_door";
    public static final String PATH = "/"
            + CONTEXT
            + "/{"
            + Params.LOGIN
            + "}/{"
            + Params.PASSWORD
            + "}/{some_int}";

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

    public static void main(String[] args) {
        try {
            final UserService userService = new UserService();
            final User user = userService.find("admin", getMD5("adminadmin"));
            System.out.println("user: " + user);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

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

                final User user__ = new User();
                user__.setActive(Boolean.TRUE);
                user__.setAvatar("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAgAAAAIACAYAAAD0eNT6AAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAALiQAAC4kBN8nLrQAAIABJREFUeJzs3XecnVWdP/DP9zy3TE8yM2mTXkhIL5NA6ERAKZa1EDZMAqIiKhZW2F1d3VWUXcsqi/5kFdeykMImtt1VgRURowQimUmFBBJSSCdt0qbduc/5/v6YoJSUKffe85TP+/XixUtfmvvJvc9zzvc55zznAERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERBQd4joAEfXMrCW22GZaqjKaqBKx1TBSYizSKpICkBKRNMRPqZU0BClRSSuQQsc/AJARIKOibVBkxGgb1MuoahuAjKhmrEEbrDarmoMpyR4qzpYcXHaLtLr7WxNRT7EAIAqgWUtscXs2M0wUI6yY4QLtD6BaVasAqQZO/lu0SiAlLjIqtBmQg1A9BMhBQA+JyCEABxXyilG7XQXbkonUyytuMC0uMhLR6bEAIHKg9gFN2uLWYSaBEWrNcBGMUNWT/5bhIhjgOmMuqWKfQLcpsF0g2xTYbsRu831sNy1FLzfcJu2uMxLFDQsAonxSlWkPNw8EzGRjZTKAyQAmqWCcQJKu4wWBQtsF2ADFehWsA3Sdil2/em7JXoio63xEUcUCgChHRj9i070PZSer6GQVdHT2qpNFpNJ1tjBS6CFA1oliHYD1orLuSFVi3UvXmjbX2YiigAUAUTdNXWL7eu2ZCwG5SKEXCTADHQvvKF9U21RQD8hyY3W5mvTTDXVy0HUsojBiAUDUGapSu7htLFQuAvQihVwkgjGuYxGgwIuALjdWlgO6vH5eehOnDojOjgUA0WnUPtg8FInENap6NUQvEUiV60x0dqp6UET+qJDHjMk+Wj+3ZKfrTERBxAKA6KTRj9h078a2S6yaawS4BoJxrjNRz6nq8xB5TK199HhV+imuISDqwAKAYu28h1pGZD3vGoFeo9C3uHqnngpFmxTyBCCP+cZ/dO3c4u2uExG5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");
                user__.setDocument(null);
                user__.setEmail(null);
                user__.setFirstName("Admin");
                user__.setLastName(null);
                user__.setInsertionDate(Long.MIN_VALUE);
                user__.setLogin("admin");
                user__.setPassword(getMD5("adminadmin"));
                user__.setType(UserType.TESTER);

                userService.insert(user__);

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

    protected static String getMD5(final String string) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(string.getBytes());
        final byte[] digest = md.digest();
        final String myHash = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        return myHash;
    }

    @Override
    protected void onClose(final SessionData sessionData, final CloseReason closeReason) {
        System.out.println("OnClose: " + closeReason.getCloseCode() + "|" + closeReason.getCloseCode().getCode() + "|" + closeReason.getReasonPhrase());
    }

    @Override
    protected void onMessage(final SessionData sessionData, final Message message) {

        try {

            System.out.println("OnMessage: " + message.getData());
            sessionData.sendObject(message);

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
