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
import com.makesystem.pidgey.lang.ThrowableHelper;
import com.makesystem.xeonentity.core.types.DatabaseType;

/**
 *
 * @author Richeli.vargas
 */
public class UserActionService extends OneService {

    public interface UserActionRunnable<R> {
        public R run() throws Throwable;
    }


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
        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_ACTION__INSERT);
            mongoConnection.getQuery().insertOneAndRetrive(action);
            return Void;
        });
    }

    public UserAction update(final UserAction action) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_ACTION__UPDATE);
            mongoConnection.getQuery().replaceOne(action);
            return action;
        });
    }

    public <R> R execute(
            final SimpleObjectId userId,
            final String userLocalIp,
            final String userPublicIp,
            final Action action,
            final UserActionRunnable<R> runnable) throws Throwable {

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
            // Run
            final R result = runnable.run();
            // Update Action
            userAction.setStatus(ActionStatus.SUCCESS);
            //
            return result;
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
