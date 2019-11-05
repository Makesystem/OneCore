/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.2
 */
package com.makesystem.onecore.services.core.customers;

import com.makesystem.mdbc.architectures.mongo.MongoConnection;
import com.makesystem.mdbc.architectures.mongo.MongoQueryBuilder;
import com.makesystem.mdbc.architectures.mongo.model.FindOptions;
import com.makesystem.onecore.services.core.OneService;
import com.makesystem.oneentity.core.nosql.Struct;
import com.makesystem.oneentity.core.types.DatabaseType;
import com.makesystem.oneentity.services.customers.storage.Customer;
import com.makesystem.pidgey.lang.StringHelper;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import java.util.Collection;
import java.util.Objects;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Richeli.vargas
 */
public class CustomerService extends OneService {

    public void insert(final Customer customer) throws Throwable {
        run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.CUSTOMER__INSERT);

            // /////////////////////////////////////////////////////////////////
            // Create the query
            // /////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

            // /////////////////////////////////////////////////////////////////
            // Create the filter
            // /////////////////////////////////////////////////////////////////
            final Bson filter = new Document(Struct.CUSTOMERS__DOCUMENT, customer.getDocument());

            // /////////////////////////////////////////////////////////////////
            // Options to insert if not exists
            // /////////////////////////////////////////////////////////////////
            final FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
            options.upsert(Boolean.TRUE);

            // /////////////////////////////////////////////////////////////////
            // Insert if not exists
            // /////////////////////////////////////////////////////////////////
            final Bson customerAsBson = mongoConnection.getQuery().wrapper().write(customer);
            final Bson setOnInsert = queryBuilder.setOnInsert(customerAsBson);

            // /////////////////////////////////////////////////////////////////
            // If any customer is already using the login or email entered, that customer 
            // will be returned.
            //
            // If login and email are not in use, the new customer will be entered 
            // and the return will be 'null';
            // /////////////////////////////////////////////////////////////////
            final Customer customerAlreadyRegistered = mongoConnection.getQuery()
                    .findOneAndUpdate(Customer.class, filter, setOnInsert, options);

            // New customer was has been entered
            if (customerAlreadyRegistered == null) {
                return Void;
            }

            if (Objects.equals(customer.getDocument(), customerAlreadyRegistered.getDocument())) {
                throw new IllegalArgumentException("Document '" + customer.getDocument() + "' is already in use");
            }

            return Void;

        });
    }

    public boolean documentAvaliable(final String document) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.CUSTOMER__IS_DOCUMENT_AVALIABLE);

            // /////////////////////////////////////////////////////////////////
            // Create the query
            // /////////////////////////////////////////////////////////////////
            final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

            // /////////////////////////////////////////////////////////////////
            // Create the filter
            // /////////////////////////////////////////////////////////////////
            final Bson documentFilter = queryBuilder.equal(Struct.CUSTOMERS__DOCUMENT, document.toLowerCase());

            // /////////////////////////////////////////////////////////////////
            // Execute
            // /////////////////////////////////////////////////////////////////
            return !mongoConnection.getQuery().exists(Customer.class, documentFilter);
        });
    }

    public Collection<Customer> find(final String text) throws Throwable {
        return run(DatabaseType.ONE, (final MongoConnection mongoConnection) -> {
            mongoConnection.setOperationAlias(OperationAlias.CUSTOMER__FIND);

            // ///////////////////////////////////////////////////////////////////////////
            // Create the query
            // ///////////////////////////////////////////////////////////////////////////
            final FindOptions findOptions = new FindOptions();

            if (!StringHelper.isBlank(text)) {
                
                final MongoQueryBuilder queryBuilder = new MongoQueryBuilder();

                final Bson document = queryBuilder.contains(Struct.CUSTOMERS__DOCUMENT, text);
                final Bson corporateName = queryBuilder.contains(Struct.CUSTOMERS__CORPORATE_NAME, text);
                final Bson fancyName = queryBuilder.contains(Struct.CUSTOMERS__FANCY_NAME, text);

                final Bson filter = queryBuilder.or(document, corporateName, fancyName);

                findOptions.setFilter(filter);
            }
            // ///////////////////////////////////////////////////////////////////////////
            // Execute
            // ///////////////////////////////////////////////////////////////////////////                    
            return mongoConnection.getQuery().find(Customer.class, findOptions);
        });
    }

}
