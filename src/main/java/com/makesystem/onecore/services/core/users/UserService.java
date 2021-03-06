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
import com.makesystem.pidgey.lang.StringHelper;
import com.makesystem.xeonentity.core.types.UserType;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.TextSearchOptions;
import java.util.Collection;
import java.util.Objects;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Richeli.vargas
 */
public class UserService extends OneService {

    private static final long serialVersionUID = -5551634350488712507L;

    private static final UserService INSTANCE = new UserService();

    public static UserService getInstance() {
        return INSTANCE;
    }

    private UserService() {
    }

    public void insert(final User user) throws Throwable {
        run((final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__INSERT);

            final boolean isUser = Objects.equals(user.getType(), UserType.CUSTOMER);

            // /////////////////////////////////////////////////////////////////
            // Create the query
            // /////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

            // /////////////////////////////////////////////////////////////////
            // Create the filter
            // /////////////////////////////////////////////////////////////////
            final Bson login = new Document(Struct.USERS__LOGIN, user.getLogin());
            final Bson email = new Document(Struct.USERS__EMAIL, user.getEmail());
            final Bson filter;

            // Users cannot have the same document.
            if (isUser) {
                final Bson document = new Document(Struct.USERS__DOCUMENT, user.getDocument());
                filter = queryBuilder.or(document, login, email);
            } else {
                filter = queryBuilder.or(login, email);
            }

            // /////////////////////////////////////////////////////////////////
            // Options to insert if not exists
            // /////////////////////////////////////////////////////////////////
            final FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
            options.upsert(Boolean.TRUE);

            // /////////////////////////////////////////////////////////////////
            // Insert if not exists
            // /////////////////////////////////////////////////////////////////
            final Bson userAsBson = mongoConnection.getQuery().wrapper().write(user);
            final Bson setOnInsert = queryBuilder.setOnInsert(userAsBson);

            // /////////////////////////////////////////////////////////////////
            // If any user is already using the login or email entered, that user 
            // will be returned.
            //
            // If login and email are not in use, the new user will be entered 
            // and the return will be 'null';
            // /////////////////////////////////////////////////////////////////
            final User userAlreadyRegistered = mongoConnection.getQuery()
                    .findOneAndUpdate(User.class, filter, setOnInsert, options);

            // New user was has been entered
            if (userAlreadyRegistered == null) {
                return;
            }

            if (Objects.equals(user.getLogin(), userAlreadyRegistered.getLogin())) {
                throw new IllegalArgumentException("Login '" + user.getLogin() + "' is already in use");
            }

            if (Objects.equals(user.getEmail(), userAlreadyRegistered.getEmail())) {
                throw new IllegalArgumentException("E-mail '" + user.getEmail() + "' is already in use");
            }

            if (Objects.equals(user.getDocument(), userAlreadyRegistered.getDocument())) {
                throw new IllegalArgumentException("Document '" + user.getDocument() + "' is already in use");
            }

        });
    }

    public boolean documentAvaliable(final String document) throws Throwable {
        return run((final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__IS_DOCUMENT_AVALIABLE);

            // /////////////////////////////////////////////////////////////////
            // Create the query
            // /////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

            // /////////////////////////////////////////////////////////////////
            // Create the filter
            // /////////////////////////////////////////////////////////////////
            final Bson documentFilter = queryBuilder.equal(Struct.USERS__DOCUMENT, document.toLowerCase());

            // /////////////////////////////////////////////////////////////////
            // Execute
            // /////////////////////////////////////////////////////////////////
            return !mongoConnection.getQuery().exists(User.class, documentFilter);
        });
    }

    public boolean loginAvaliable(final String login) throws Throwable {
        return run((final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__IS_LOGIN_AVALIABLE);

            // /////////////////////////////////////////////////////////////////
            // Create the query
            // /////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();
            final Bson loginFilter = queryBuilder.equal(Struct.USERS__LOGIN, login.toLowerCase());

            // /////////////////////////////////////////////////////////////////
            // Execute
            // /////////////////////////////////////////////////////////////////
            return !mongoConnection.getQuery().exists(User.class, loginFilter);
        });
    }

    public boolean emailAvaliable(final String email) throws Throwable {
        return run((final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__IS_EMAIL_AVALIABLE);

            // ///////////////////////////////////////////////////////////////////////////
            // Create the query
            // ///////////////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

            // /////////////////////////////////////////////////////////////////
            // Create the filter
            // /////////////////////////////////////////////////////////////////            
            final Bson emailFilter = queryBuilder.equal(Struct.USERS__EMAIL, email.toLowerCase());

            // ///////////////////////////////////////////////////////////////////////////
            // Execute
            // ///////////////////////////////////////////////////////////////////////////
            return !mongoConnection.getQuery().exists(User.class, emailFilter);
        });
    }

    public User find(final String loginOrEmail, final String password) throws Throwable {
        return run((final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__FIND_BY__LOGIN_AND_PASSWORD);

            // /////////////////////////////////////////////////////////////////
            // Create the query
            // /////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

            // /////////////////////////////////////////////////////////////////
            // Create the filter
            // /////////////////////////////////////////////////////////////////            
            final Bson loginFilter = queryBuilder.equal(Struct.USERS__LOGIN, loginOrEmail.toLowerCase());
            final Bson emailFilter = queryBuilder.equal(Struct.USERS__EMAIL, loginOrEmail.toLowerCase());
            final Bson passwordFilter = queryBuilder.equal(Struct.USERS__PASSWORD, password);
            final Bson loginOrEmailFilter = queryBuilder.or(loginFilter, emailFilter);
            final Bson filter = queryBuilder.and(loginOrEmailFilter, passwordFilter);

            final FindOptions findOptions = new FindOptions();
            findOptions.setFilter(filter);
            findOptions.setLimit(1);

            // /////////////////////////////////////////////////////////////////
            // Execute
            // /////////////////////////////////////////////////////////////////
            final Collection<User> connections
                    = mongoConnection.getQuery().find(User.class, findOptions);

            return connections.isEmpty() ? null : connections.iterator().next();
        });
    }

    public Collection<User> find(final String text) throws Throwable {
        return run((final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.USER__FIND);

            // ///////////////////////////////////////////////////////////////////////////
            // Create the query
            // ///////////////////////////////////////////////////////////////////////////
            final FindOptions findOptions = new FindOptions();

            if (!StringHelper.isBlank(text)) {

                final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

                final TextSearchOptions options = new TextSearchOptions()
                        .caseSensitive(Boolean.FALSE)
                        .diacriticSensitive(Boolean.FALSE);
                final Bson filter = queryBuilder.text(text, options);

                findOptions.setFilter(filter);

            }
            // ///////////////////////////////////////////////////////////////////////////
            // Execute
            // ///////////////////////////////////////////////////////////////////////////                    
            return mongoConnection.getQuery().find(User.class, findOptions);
        });
    }

}
