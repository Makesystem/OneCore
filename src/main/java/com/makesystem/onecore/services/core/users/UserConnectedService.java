/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.2
 */
package com.makesystem.onecore.services.core.users;

import com.makesystem.mdbc.architectures.mongo.MongoConnection;
import com.makesystem.mdbc.architectures.mongo.MongoQueryBuilder;
import com.makesystem.mdbc.architectures.mongo.model.FindOptions;
import com.makesystem.mdbi.nosql.SimpleObjectId;
import com.makesystem.onecore.services.core.OneService;
import com.makesystem.oneentity.core.nosql.Struct;
import com.makesystem.oneentity.core.types.DatabaseType;
import com.makesystem.oneentity.core.types.ServiceType;
import com.makesystem.oneentity.services.users.storage.UserConnected;
import java.util.Collection;
import java.util.LinkedList;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Richeli.vargas
 */
public class UserConnectedService extends OneService {

    private static final UserConnectedService INSTANCE = new UserConnectedService();
    
    public static UserConnectedService getInstance(){
        return INSTANCE;
    }
    
    private UserConnectedService(){}
    
    public UserConnected insert(final UserConnected user) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_CONNECTED__INSERT);
            return mongoConnection.getQuery().insertOneAndRetrive(user);
        });
    }

    public void delete(final UserConnected user) throws Throwable {

        if (user == null) {
            return;
        }

        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_CONNECTED__DELETE);
            mongoConnection.getQuery().delete(user);
        });
    }

    public void delete(final SimpleObjectId userId) throws Throwable {

        if (userId == null) {
            return;
        }

        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_CONNECTED__DELETE);
            mongoConnection.getQuery().deleteMany(UserConnected.class, new Document(Struct.USERS_CONNECTED__USER, userId));
        });
    }

    public void delete(final String serverName) throws Throwable {

        if (serverName == null) {
            return;
        }

        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_CONNECTED__DELETE);
            mongoConnection.getQuery().deleteMany(UserConnected.class, new Document(Struct.USERS_CONNECTED__SERVER_NAME, serverName));
        });
    }

    public void clear() throws Throwable {
        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_CONNECTED__CLEAR);
            mongoConnection.getMongoDatabase(Struct.USERS_CONNECTED__TABLE_NAME).drop();
        });
    }

    public int count() throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_CONNECTED__COUNT);
            return (int) mongoConnection.getQuery().count(UserConnected.class, new Document());
        });
    }

    public Collection<UserConnected> find(final String user, final String customer, final ServiceType service) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER_CONNECTED__FIND);

            // ///////////////////////////////////////////////////////////////////////////
            // Create the query
            // ///////////////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

            final Collection<Bson> filters = new LinkedList<>();

            if (user != null && !user.trim().isEmpty()) {
                filters.add(queryBuilder.equal(Struct.USERS_CONNECTED__USER, user));
            }

            if (customer != null && !customer.trim().isEmpty()) {
                filters.add(queryBuilder.equal(Struct.USERS_CONNECTED__CUSTOMER, customer));
            }

            if (service != null) {
                filters.add(queryBuilder.equal(Struct.USERS_CONNECTED__SERVICE, service));
            }

            final Bson filter = queryBuilder.and(filters.stream().toArray(Bson[]::new));

            final FindOptions findOptions = new FindOptions();
            findOptions.setFilter(filter);
            findOptions.setLimit(1);

            // ///////////////////////////////////////////////////////////////////////////
            // Execute
            // ///////////////////////////////////////////////////////////////////////////                    
            final Collection<UserConnected> connections
                    = mongoConnection.getQuery().find(UserConnected.class, findOptions);

            return connections;
        });
    }

}
