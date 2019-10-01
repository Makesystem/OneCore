/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.onecore.services.core.users.ConnectedUserService;
import com.makesystem.oneentity.core.types.Action;
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

    public <D> String consumer(final SessionData sessionData, final Message message) throws Throwable {
        switch (Action.valueOf(message.getAction())) {
            case ONE__ECHO:
                return one__echo(message.getData());
            case ONE__CURRENT_TIMESTAMP:
                return one__current_timestamp(message.getData());
            case MANAGEMENT__SERVER_AVERAGE:
                return management__server_average(message.getData());
            case MANAGEMENT__DATABASE_AVERAGE:
                return management__database_average(message.getData());
            case MANAGEMENT__ALIAS_AVERAGE:
                return management__alias_average(message.getData());
            default:
                throw new IllegalArgumentException("Unknow action: " + message.getAction());
        }
    }

    /**
     *
     * @param <D>
     * @param data
     * @return The message data
     */
    protected <D> String one__echo(final D data) {
        return String.valueOf(data);
    }

    protected <D> String one__current_timestamp(final D data) {
        return String.valueOf(System.currentTimeMillis());
    }
    
    protected <D> String one__connected_user__count(final D data) throws Throwable{
        
        final ConnectedUserService service = new ConnectedUserService();        
        
        return ObjectMapperJRE.write(service.count());
    }

    protected <D> String management__server_average(final D data) throws Throwable {

        final long openAtMax = System.currentTimeMillis();
        final long openAtMin = openAtMax - NumberHelper.toMillis(1, TimeUnit.HOURS);
        
        final StatisticsService service = new StatisticsService();
        final Collection<ServerAvg> result = service.getServerAvg(openAtMin, openAtMax);
                
        return ObjectMapperJRE.write(result);
    }
    
    protected <D> String management__database_average(final D data) throws Throwable {

        final long openAtMax = System.currentTimeMillis();
        final long openAtMin = openAtMax - NumberHelper.toMillis(1, TimeUnit.HOURS);
        
        final StatisticsService service = new StatisticsService();
        final Collection<DatabaseAvg> result = service.getDatabaseAvg(openAtMin, openAtMax);
                
        return ObjectMapperJRE.write(result);
    }
    
    protected <D> String management__alias_average(final D data) throws Throwable {

        final String database = data.toString();
        final long openAtMax = System.currentTimeMillis();
        final long openAtMin = openAtMax - NumberHelper.toMillis(1, TimeUnit.HOURS);
        
        final StatisticsService service = new StatisticsService();
        final Collection<AliasAvg> result = service.getAliasAvg(database, openAtMin, openAtMax);
                
        return ObjectMapperJRE.write(result);
    }
}
