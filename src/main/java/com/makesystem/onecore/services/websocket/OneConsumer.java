/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.onecore.services.core.users.ConnectedUserService;
import com.makesystem.oneentity.core.types.Action;
import com.makesystem.pidgey.interfaces.AsyncCallback;
import com.makesystem.pidgey.json.ObjectMapperJRE;
import com.makesystem.pidgey.lang.NumberHelper;
import com.makesystem.xeoncore.services.management.StatisticsService;
import com.makesystem.xeonentity.core.websocket.Message;
import com.makesystem.xeonentity.services.management.runnable.AliasAvg;
import com.makesystem.xeonentity.services.management.runnable.DatabaseAvg;
import com.makesystem.xeonentity.services.management.runnable.ServerAvg;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Richeli.vargas
 */
public class OneConsumer {

    public <D> void consumer(final SessionData sessionData, final Message message, final AsyncCallback<String> callback) {

        try {

            switch (Action.valueOf(message.getAction())) {
                case ONE__ECHO:
                    one__echo(message.getData(), callback);
                case ONE__CURRENT_TIMESTAMP:
                    one__current_timestamp(message.getData(), callback);
                case MANAGEMENT__SERVER_AVERAGE:
                    management__server_average(message.getData(), callback);
                case MANAGEMENT__DATABASE_AVERAGE:
                    management__database_average(message.getData(), callback);
                case MANAGEMENT__ALIAS_AVERAGE:
                    management__alias_average(message.getData(), callback);
                default:
                    throw new IllegalArgumentException("Unknow action: " + message.getAction());
            }

        } catch (final Throwable throwable) {
            callback.onFailure(throwable);
        }
    }

    /**
     *
     * @param <D>
     * @param data
     * @param callback
     */
    protected <D> void one__echo(final D data, final AsyncCallback<String> callback) {
        callback.onSuccess(String.valueOf(data));
    }

    protected <D> void one__current_timestamp(final D data, final AsyncCallback<String> callback) {
        callback.onSuccess(String.valueOf(System.currentTimeMillis()));
    }

    protected <D> void one__connected_user__count(final D data, final AsyncCallback<String> callback) throws Throwable {

        final ConnectedUserService service = new ConnectedUserService();

        callback.onSuccess(ObjectMapperJRE.write(service.count()));
    }

    protected <D> void management__server_average(final D data, final AsyncCallback<String> callback) throws Throwable {

        final long openAtMax = System.currentTimeMillis();
        final long openAtMin = openAtMax - NumberHelper.toMillis(1, TimeUnit.HOURS);

        final StatisticsService service = new StatisticsService();
        final Collection<ServerAvg> result = service.getServerAvg(openAtMin, openAtMax);

        callback.onSuccess(ObjectMapperJRE.write(result));
    }

    protected <D> void management__database_average(final D data, final AsyncCallback<String> callback) throws Throwable {

        final long openAtMax = System.currentTimeMillis();
        final long openAtMin = openAtMax - NumberHelper.toMillis(1, TimeUnit.HOURS);

        final StatisticsService service = new StatisticsService();
        final Collection<DatabaseAvg> result = service.getDatabaseAvg(openAtMin, openAtMax);

        callback.onSuccess(ObjectMapperJRE.write(result));
    }

    protected <D> void management__alias_average(final D data, final AsyncCallback<String> callback) throws Throwable {

        final String database = data.toString();
        final long openAtMax = System.currentTimeMillis();
        final long openAtMin = openAtMax - NumberHelper.toMillis(1, TimeUnit.HOURS);

        final StatisticsService service = new StatisticsService();
        final Collection<AliasAvg> result = service.getAliasAvg(database, openAtMin, openAtMax);

        callback.onSuccess(ObjectMapperJRE.write(result));
    }
}
