/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.websocket;

import com.makesystem.mwc.websocket.server.SessionData;
import com.makesystem.oneentity.core.websocket.Message;
import com.makesystem.pidgey.json.ObjectMapperJRE;
import com.makesystem.xeoncore.services.management.databaseStatisticService.DatabaseStatisticService;
import com.makesystem.xeonentity.services.management.DatabaseConnections;
import com.makesystem.xeonentity.services.management.DatabaseStatistic;

/**
 *
 * @author Richeli.vargas
 */
public class OneConsumer {

    public <D> String consumer(final SessionData sessionData, final Message message) throws Throwable {
        switch (message.getAction()) {
            case ONE__ECHO:
                return one__echo(message.getData());
            case ONE__CURRENT_TIMESTAMP:
                return one__current_timestamp(message.getData());
            case ONE__DATABASE_STATISTICS:
                return one__database_statistics(message.getData());
            case ONE__DATABASE_CONNECTIONS:
                return one__database_connections(message.getData());
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

    protected <D> String one__database_statistics(final D data) throws Throwable {

        final long timestamp = System.currentTimeMillis();
        //final long openAtMin = timestamp - (timestamp % (24 * 60 * 60 * 1000));
        // Last 24 hours
        final long openAtMin = timestamp - (24 * 60 * 60 * 100);
        final long openAtMax = timestamp;
        final int durationAbove = 100/*ms*/; 
        final int limitOfLongOperations = 10;

        final DatabaseStatisticService databaseStatisticService = new DatabaseStatisticService();
        final DatabaseStatistic databaseStatistic = databaseStatisticService.getDatabaseStatistic(
                openAtMin, 
                openAtMax, 
                durationAbove, 
                limitOfLongOperations);

        return ObjectMapperJRE.write(databaseStatistic);
    }
    
    protected <D> String one__database_connections(final D data) throws Throwable {

        final long timestamp = System.currentTimeMillis();
        //final long openAtMin = timestamp - (timestamp % (24 * 60 * 60 * 1000));
        // Last 24 hours
        final long openAtMin = timestamp - (24 * 60 * 60 * 100);
        final long openAtMax = timestamp;
        
        final DatabaseStatisticService databaseStatisticService = new DatabaseStatisticService();
        final DatabaseConnections databaseConnections = databaseStatisticService.getDatabaseConnections(openAtMin, openAtMax);

        return ObjectMapperJRE.write(databaseConnections);
    }
}
