/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core.access;

import com.makesystem.mwc.http.server.RequestData;
import com.makesystem.mwc.websocket.server.PathParameters;
import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.mwi.exceptions.RequestException;
import com.makesystem.mwi.websocket.CloseReason;
import com.makesystem.onecore.services.core.OneProperties;
import com.makesystem.onecore.services.core.OneUser;
import com.makesystem.onecore.services.core.users.UserActionService;
import com.makesystem.onecore.services.core.users.UserConnectedService;
import com.makesystem.onecore.services.core.users.UserService;
import com.makesystem.oneentity.core.types.OneCloseCodes;
import com.makesystem.oneentity.core.types.ServiceType;
import com.makesystem.oneentity.services.OneServices;
import com.makesystem.oneentity.services.users.storage.User;
import com.makesystem.oneentity.services.users.storage.UserConnected;
import java.io.Serializable;

/**
 *
 * @author riche
 */
public class LoginCtrl implements Serializable {

    private static final long serialVersionUID = -3681774837747314029L;

    private final UserConnectedService connectedUserService;
    private final UserService userService;
    private final UserActionService userActionService;

    private static final LoginCtrl INSTANCE = new LoginCtrl();
    
    public static LoginCtrl getInstance(){
        return INSTANCE;
    }
    
    private LoginCtrl() {
        this.connectedUserService = UserConnectedService.getInstance();
        this.userService = UserService.getInstance();
        this.userActionService = UserActionService.getInstance();
    }
    
    public void doStartUp() throws Throwable {
        this.connectedUserService.delete(OneProperties.SERVER_NAME.getValue());
    }

    public OneUser doLogin(final SessionData sessionData) throws Throwable {

        final long startAction = System.currentTimeMillis();

        // OnOpen parameters
        final PathParameters parameters = sessionData.getParameters();

        // Request data from ServletRequest
        final RequestData requestData = sessionData.getRequestData();

        // Client        
        final String loginOrEmail = parameters.getString(OneServices.Access.Attributes.LOGIN);
        final String password = parameters.getString(OneServices.Access.Attributes.PASSWORD);
        final ServiceType service = parameters.getEnum(ServiceType.class, OneServices.Access.Attributes.SERVICE);

        final String customer = null;
        final String localIp = requestData.getRemoteHost();
        final String publicIp = requestData.getRemoteHost();

        //Server
        final String serverName = OneProperties.SERVER_NAME.getValue();
        final String httpHost = requestData.getServerHost();
        final Integer httpPort = requestData.getServerPort();

        // Find user by ((login or e-mail) and password)
        final User user = userService.find(loginOrEmail, password);

        if (user == null) {
            throw new RequestException(
                    OneCloseCodes.LOGIN_OR_PASSWORD_IS_INVALID.getCode(),
                    "Login or e-mail is wrong");
        }

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
        connectedUser.setService(service);
        connectedUser.setInsertionDate(startAction);

        final UserConnected connection = connectedUserService.insert(connectedUser);

        // /////////////////////////////////////////////////////////////
        // Create OneUser data
        // /////////////////////////////////////////////////////////////        
        final OneUser oneUser = new OneUser(user, connection);
        
        // /////////////////////////////////////////////////////////////
        // Set session User
        // /////////////////////////////////////////////////////////////
        sessionData.setData(oneUser);

        // /////////////////////////////////////////////////////////////
        // Save login action
        // /////////////////////////////////////////////////////////////
        userActionService.insertLoginAction(oneUser, startAction);

        // /////////////////////////////////////////////////////////////
        // Return oneUser data
        // /////////////////////////////////////////////////////////////        
        return oneUser;
    }

    public void doLogoff(final SessionData sessionData, final CloseReason closeReason) throws Throwable {

        final OneUser user = sessionData.getData();

        if (user == null) {
            return;
        }

        final long startAction = System.currentTimeMillis();

        // /////////////////////////////////////////////////////////////////////
        // Remove connection register
        // /////////////////////////////////////////////////////////////////////
        final UserConnected connection = user.getConnection();

        try {

            // /////////////////////////////////////////////////////////////
            // Remove connection register
            // /////////////////////////////////////////////////////////////        
            connectedUserService.delete(connection);

        } finally {

            // /////////////////////////////////////////////////////////////
            // Save login action
            // /////////////////////////////////////////////////////////////
            userActionService.insertLoginAction(user, startAction);
        }

    }
}
