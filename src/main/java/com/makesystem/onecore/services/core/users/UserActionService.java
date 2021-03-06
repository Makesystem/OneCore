/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.2
 */
package com.makesystem.onecore.services.core.users;

import com.makesystem.mdbc.architectures.mongo.MongoConnection;
import com.makesystem.mdbi.nosql.SimpleObjectId;
import com.makesystem.onecore.services.core.OneService;
import com.makesystem.onecore.services.core.OneUser;
import com.makesystem.oneentity.core.types.Action;
import com.makesystem.oneentity.core.types.ActionStatus;
import com.makesystem.oneentity.services.users.storage.UserAction;
import com.makesystem.pidgey.interfaces.Snippet;
import com.makesystem.pidgey.lang.ThrowableHelper;

/**
 *
 * @author Richeli.vargas
 */
public class UserActionService extends OneService {

    private static final long serialVersionUID = 2176453190818836647L;

    private static final UserActionService INSTANCE = new UserActionService();
    
    public static UserActionService getInstance(){
        return INSTANCE;
    }
    
    private UserActionService(){}
    
    public void insertLoginAction(final OneUser user, final long startAction) throws Throwable {
        final UserAction userAction = new UserAction();
        userAction.setAction(Action.ONE__LOGIN);
        userAction.setCustomer(null);
        userAction.setInsertionDate(startAction);
        userAction.setUser(user.getUser().getId());
        userAction.setLocalIp(user.getLocalIp());
        userAction.setPublicIp(user.getPublicIp());
        userAction.setStatus(ActionStatus.SUCCESS);
        userAction.setDurarion((int) (System.currentTimeMillis() - startAction));
        this.insert(userAction);
    }
    
    public void insertLogoffAction(final OneUser user, final long startAction) throws Throwable {
        final UserAction userAction = new UserAction();
        userAction.setAction(Action.ONE__LOGOFF);
        userAction.setCustomer(null);
        userAction.setInsertionDate(startAction);
        userAction.setUser(user.getUser().getId());
        userAction.setLocalIp(user.getLocalIp());
        userAction.setPublicIp(user.getPublicIp());
        userAction.setStatus(ActionStatus.SUCCESS);
        userAction.setDurarion((int) (System.currentTimeMillis() - startAction));
        this.insert(userAction);
    }

    public void insert(final UserAction action) throws Throwable {
        run((final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_ACTION__INSERT);
            mongoConnection.getQuery().insertOneAndRetrive(action);
        });
    }

    public UserAction update(final UserAction action) throws Throwable {
        return run((final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_ACTION__UPDATE);
            mongoConnection.getQuery().replaceOne(action);
            return action;
        });
    }

    public <R> void execute(
            final SimpleObjectId userId,
            final String userLocalIp,
            final String userPublicIp,
            final Action action,
            final Snippet snippet) throws Throwable {

        final UserAction userAction = new UserAction();
        userAction.setUser(userId);
        userAction.setAction(action);
        userAction.setCustomer(null);
        userAction.setInsertionDate(System.currentTimeMillis());
        userAction.setLocalIp(userLocalIp);
        userAction.setPublicIp(userPublicIp);
        userAction.setStatus(ActionStatus.RUNNING);
        this.insert(userAction);

        try {
            // Execute
            snippet.exec();
            // Update Action
            userAction.setStatus(ActionStatus.SUCCESS);
        } catch (final Throwable throwable) {
            // Update Action
            userAction.setStatus(ActionStatus.ERROR);
            userAction.setError(ThrowableHelper.toString(throwable));
            throw throwable;
        } finally {
            // Update action info
            userAction.setDurarion((int) (System.currentTimeMillis() - userAction.getInsertionDate()));
            this.update(userAction);
        }

    }

}
