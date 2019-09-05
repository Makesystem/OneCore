/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.2
 */
package com.makesystem.onecore.services.core.users;

import com.makesystem.mdbc.architectures.mongo.MongoConnection;
import com.makesystem.mdbc.architectures.mongo.MongoQueryBuilder;
import com.makesystem.mdbc.architectures.mongo.model.FindOptions;
import com.makesystem.onecore.services.core.OneService;
import com.makesystem.oneentity.core.nosql.Struct;
import com.makesystem.oneentity.services.users.storage.User;
import com.makesystem.xeonentity.core.types.DatabaseType;
import java.util.Collection;
import org.bson.conversions.Bson;

/**
 *
 * @author Richeli.vargas
 */
public class UserService extends OneService {

    public User insert(final User user) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__INSERT);
            
            if(!loginAvaliable(user.getLogin())){       
                throw new IllegalArgumentException("Login '" + user.getLogin() + "' is already in use");
            }
            
            if(!emailAvaliable(user.getEmail())){
                throw new IllegalArgumentException("E-mail '" + user.getEmail() + "' is already in use");
            }
            
            return mongoConnection.getQuery().insert(user);                        
        });
    }
    
    public Collection<User> insert(final Collection<User> users) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__INSERT);
            
            mongoConnection.getQuery().insert(users);     
            
            return users;                   
        });
    }
    
    
    public boolean loginAvaliable(final String login) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__FIND_BY__LOGIN_AND_PASSWORD);

            // ///////////////////////////////////////////////////////////////////////////
            // Create the query
            // ///////////////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();
            final Bson loginFilter = queryBuilder.equal(Struct.USERS__LOGIN, login.toLowerCase());
         
            // ///////////////////////////////////////////////////////////////////////////
            // Execute
            // ///////////////////////////////////////////////////////////////////////////
            return !mongoConnection.getQuery().exists(User.class, loginFilter);
        });
    }
    
    public boolean emailAvaliable(final String email) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__FIND_BY__LOGIN_AND_PASSWORD);

            // ///////////////////////////////////////////////////////////////////////////
            // Create the query
            // ///////////////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();
            final Bson emailFilter = queryBuilder.equal(Struct.USERS__EMAIL, email.toLowerCase());
         
            // ///////////////////////////////////////////////////////////////////////////
            // Execute
            // ///////////////////////////////////////////////////////////////////////////
            return !mongoConnection.getQuery().exists(User.class, emailFilter);
        });
    }
    
    public User find(final String loginOrEmail, final String password) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__FIND_BY__LOGIN_AND_PASSWORD);

            // ///////////////////////////////////////////////////////////////////////////
            // Create the query
            // ///////////////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

            final Bson loginFilter = queryBuilder.equal(Struct.USERS__LOGIN, loginOrEmail.toLowerCase());
            final Bson emailFilter = queryBuilder.equal(Struct.USERS__EMAIL, loginOrEmail.toLowerCase());
            final Bson passwordFilter = queryBuilder.equal(Struct.USERS__PASSWORD, password);
            final Bson loginOrEmailFilter = queryBuilder.or(loginFilter, emailFilter);
            final Bson filter = queryBuilder.and(loginOrEmailFilter, passwordFilter);
            
            final FindOptions findOptions = new FindOptions();
            findOptions.setFilter(filter);
            findOptions.setLimit(1);

            // ///////////////////////////////////////////////////////////////////////////
            // Execute
            // ///////////////////////////////////////////////////////////////////////////                    
            final Collection<User> connections
                    = mongoConnection.getQuery().find(User.class, findOptions);

            return connections.isEmpty() ? null : connections.iterator().next();
        });
    }

}
