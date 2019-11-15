/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.onecore.services.core.OneRequestService;
import com.makesystem.onecore.services.core.OneUser;
import com.makesystem.onecore.services.core.users.UserConnectedService;
import com.makesystem.oneentity.core.types.Action;
import com.makesystem.oneentity.services.request.runnable.RequestData;
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
                case ONE__REQUEST_SERVICE:
                    one__request_service(sessionData, message.getData(), callback);
                    break;
                case ONE__ECHO:
                    one__echo(message.getData(), callback);
                    break;
                case ONE__CURRENT_TIMESTAMP:
                    one__current_timestamp(message.getData(), callback);
                    break;
                case MANAGEMENT__SERVER_AVERAGE:
                    management__server_average(message.getData(), callback);
                    break;
                case MANAGEMENT__DATABASE_AVERAGE:
                    management__database_average(message.getData(), callback);
                    break;
                case MANAGEMENT__ALIAS_AVERAGE:
                    management__alias_average(message.getData(), callback);
                    break;
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
     * @param sessionData
     * @param data
     * @param callback 
     * @throws Throwable 
     */
    protected <D> void one__request_service(final SessionData sessionData, final D data, final AsyncCallback<String> callback) throws Throwable {        
        final OneUser user = sessionData.getData();
        final RequestData request = ObjectMapperJRE.read(data.toString(), RequestData.class);        
        OneRequestService.getInstance().request(user, request, callback);
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

    /**
     * 
     * @param <D>
     * @param data
     * @param callback 
     */
    protected <D> void one__current_timestamp(final D data, final AsyncCallback<String> callback) {
        callback.onSuccess(String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 
     * @param <D>
     * @param data
     * @param callback
     * @throws Throwable 
     */
    protected <D> void one__connected_user__count(final D data, final AsyncCallback<String> callback) throws Throwable {
        final UserConnectedService service = UserConnectedService.getInstance();
        callback.onSuccess(ObjectMapperJRE.write(service.count()));
    }

    /**
     * 
     * @param <D>
     * @param data
     * @param callback
     * @throws Throwable 
     */
    protected <D> void management__server_average(final D data, final AsyncCallback<String> callback) throws Throwable {

        final long openAtMax = System.currentTimeMillis();
        final long openAtMin = openAtMax - NumberHelper.toMillis(1, TimeUnit.DAYS);

        final StatisticsService service = new StatisticsService();
        final Collection<ServerAvg> result = service.getServerAvg(openAtMin, openAtMax);

        callback.onSuccess(ObjectMapperJRE.write(result));
    }

    protected <D> void management__database_average(final D data, final AsyncCallback<String> callback) throws Throwable {

        final long openAtMax = System.currentTimeMillis();
        final long openAtMin = openAtMax - NumberHelper.toMillis(1, TimeUnit.DAYS);

        final StatisticsService service = new StatisticsService();
        final Collection<DatabaseAvg> result = service.getDatabaseAvg(openAtMin, openAtMax);

        callback.onSuccess(ObjectMapperJRE.write(result));
    }

    protected <D> void management__alias_average(final D data, final AsyncCallback<String> callback) throws Throwable {

        final String database = data.toString();
        final long openAtMax = System.currentTimeMillis();
        final long openAtMin = openAtMax - NumberHelper.toMillis(1, TimeUnit.DAYS);

        final StatisticsService service = new StatisticsService();
        final Collection<AliasAvg> result = service.getAliasAvg(database, openAtMin, openAtMax);

        callback.onSuccess(ObjectMapperJRE.write(result));
    }
}
