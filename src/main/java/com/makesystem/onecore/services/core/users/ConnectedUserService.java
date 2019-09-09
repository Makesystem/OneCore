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
import com.makesystem.oneentity.core.types.ServiceType;
import com.makesystem.oneentity.services.users.storage.ConnectedUser;
import com.makesystem.xeonentity.core.types.DatabaseType;
import java.util.Collection;
import java.util.LinkedList;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Richeli.vargas
 */
public class ConnectedUserService extends OneService {

    public ConnectedUser insert(final ConnectedUser user) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.CONNECTED_USER__INSERT);
            return mongoConnection.getQuery().insertOneAndRetrive(user);
        });
    }
    
    public void delete(final ConnectedUser user) throws Throwable {
        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.CONNECTED_USER__DELETE);
            mongoConnection.getQuery().delete(user);
            return Void;
        });
    }
    
    public void delete(final SimpleObjectId userId) throws Throwable {
        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.CONNECTED_USER__DELETE);
            mongoConnection.getQuery().deleteMany(ConnectedUser.class, new Document(Struct.CONNECTED_USERS__USER, userId));
            return Void;
        });
    }
    
    public void delete(final String serverHost) throws Throwable {
        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.CONNECTED_USER__DELETE);
            mongoConnection.getQuery().deleteMany(ConnectedUser.class, new Document(Struct.CONNECTED_USERS__SERVER_HOST, serverHost));
            return Void;
        });
    }
    
    public void clear() throws Throwable {
        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.CONNECTED_USER__CLEAR);
            mongoConnection.getMongoDatabase(Struct.CONNECTED_USERS__TABLE_NAME).drop();
            return Void;
        });
    }

    public Collection<ConnectedUser> find(final String user, final String customer, final ServiceType service) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.CONNECTED_USER__FIND);

            // ///////////////////////////////////////////////////////////////////////////
            // Create the query
            // ///////////////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

            final Collection<Bson> filters = new LinkedList<>();

            if (user != null && !user.trim().isEmpty()) {
                filters.add(queryBuilder.equal(Struct.CONNECTED_USERS__USER, user));
            }
            
            if (customer != null && !customer.trim().isEmpty()) {
                filters.add(queryBuilder.equal(Struct.CONNECTED_USERS__CUSTOMER, customer));
            }
            
            if (service != null) {
                filters.add(queryBuilder.equal(Struct.CONNECTED_USERS__SERVICE, service));
            }

            final Bson filter = queryBuilder.and(filters.stream().toArray(Bson[]::new));

            final FindOptions findOptions = new FindOptions();
            findOptions.setFilter(filter);
            findOptions.setLimit(1);

            // ///////////////////////////////////////////////////////////////////////////
            // Execute
            // ///////////////////////////////////////////////////////////////////////////                    
            final Collection<ConnectedUser> connections
                    = mongoConnection.getQuery().find(ConnectedUser.class, findOptions);

            return connections;
        });
    }

}
