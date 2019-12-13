/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core.upgrade;

import com.makesystem.mdbc.architectures.mongo.MongoConnection;
import com.makesystem.onecore.services.core.OneProperties;
import com.makesystem.oneentity.core.nosql.Struct;
import com.makesystem.oneentity.core.types.DatabaseType;
import com.makesystem.oneentity.services.customers.storage.Customer;
import com.makesystem.oneentity.services.users.storage.User;
import com.makesystem.oneentity.services.users.storage.UserAction;
import com.makesystem.oneentity.services.users.storage.UserConnected;
import com.makesystem.xeoncore.core.AbstractUpgradeService;
import com.makesystem.xeoncore.core.BasicUserData;
import com.makesystem.xeoncore.utils.IndexesUtils;
import com.makesystem.xeonentity.core.DatabaseSettings;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.conversions.Bson;

/**
 *
 * @author riche
 */
public class UpgradeService extends AbstractUpgradeService {

    private static final long serialVersionUID = -6725193683799153230L;

    private static final UpgradeService INSTANCE = new UpgradeService();

    public static UpgradeService getInstance() {
        return INSTANCE;
    }

    private final com.makesystem.xeoncore.services.management.UpgradeService managementUpgradeService
            = new com.makesystem.xeoncore.services.management.UpgradeService();

    private UpgradeService() {
        super(new DatabaseSettings(
                OneProperties.DATABASE__HOST.getValue(),
                OneProperties.DATABASE__PORT.getValue(),
                OneProperties.DATABASE__NAME.getValue(),
                OneProperties.DATABASE__USER.getValue(),
                OneProperties.DATABASE__PASSWORD.getValue(),
                OneProperties.DATABASE__TYPE.getValue(),
                DatabaseType.ONE,
                OneProperties.DATABASE__POOL_SIZE.getValue()));
    }

    @Override
    public void upgradeImpl(final BasicUserData basicUserData) throws Throwable {
        
        try {
            managementUpgradeService.upgradeImpl(basicUserData);
        } catch (final Throwable ignore) {
        }
        
        runTransactional(DatabaseType.ONE, basicUserData, (final MongoConnection mongoConnection) -> {
            createUserIndexes(mongoConnection);
            createUserActionIndexes(mongoConnection);
            createUserConnectedIndexes(mongoConnection);
            createCustomerIndexes(mongoConnection);
        });
    }

    /**
     *
     * @param mongoConnection
     * @throws Throwable
     */
    protected void createUserIndexes(final MongoConnection mongoConnection) throws Throwable {

        /*
         * singin index 
         */
        final Bson singinKeys = Indexes.ascending(Struct.USERS__LOGIN, Struct.USERS__EMAIL, Struct.USERS__PASSWORD);
        final IndexOptions singinOptions = new IndexOptions().name("singin");
        final IndexModel singinModel = new IndexModel(singinKeys, singinOptions);

        /*
         * login index 
         */
        final Bson loginKeys = Indexes.ascending(Struct.USERS__LOGIN);
        final IndexOptions loginOptions = new IndexOptions().name("login");
        final IndexModel loginModel = new IndexModel(loginKeys, loginOptions);

        /*
         * login index 
         */
        final Bson emailKeys = Indexes.ascending(Struct.USERS__EMAIL);
        final IndexOptions emailOptions = new IndexOptions().name("email");
        final IndexModel emailModel = new IndexModel(emailKeys, emailOptions);

        /*
         * document index 
         */
        final Bson documentKeys = Indexes.ascending(Struct.USERS__DOCUMENT);
        final IndexOptions documentOptions = new IndexOptions().name("document");
        final IndexModel documentModel = new IndexModel(documentKeys, documentOptions);

        /*
         * text index 
         */
        final Bson textKeys = Indexes.compoundIndex(
                Indexes.text(Struct.USERS__DOCUMENT),
                Indexes.text(Struct.USERS__EMAIL),
                Indexes.text(Struct.USERS__FIRST_NAME),
                Indexes.text(Struct.USERS__LAST_NAME),
                Indexes.text(Struct.USERS__LOGIN),
                Indexes.text(Struct.USERS__TYPE)
        );
        final IndexOptions textOptions = new IndexOptions().name("text");
        final IndexModel textModel = new IndexModel(textKeys, textOptions);

        IndexesUtils.createIndexes(mongoConnection,
                User.class,
                singinModel,
                loginModel,
                emailModel,
                documentModel,
                textModel);
    }

    /**
     *
     * @param mongoConnection
     * @throws Throwable
     */
    protected void createUserActionIndexes(final MongoConnection mongoConnection) throws Throwable {

        /*
         * insertionDate index 
         */
        final Bson insertionDateKeys = Indexes.descending(Struct.USERS_ACTIONS__INSERTION_DATE);
        final IndexOptions insertionDateOptions = new IndexOptions().name("insertion_date");
        final IndexModel insertionDateModel = new IndexModel(insertionDateKeys, insertionDateOptions);

        /*
         * duration index 
         */
        final Bson durationKeys = Indexes.descending(Struct.USERS_ACTIONS__DURATION);
        final IndexOptions durationOptions = new IndexOptions().name("duration");
        final IndexModel durationModel = new IndexModel(durationKeys, durationOptions);

        /*
         * customer index 
         */
        final Bson customerKeys = Indexes.ascending(Struct.USERS_ACTIONS__CUSTOMER);
        final IndexOptions customerOptions = new IndexOptions().name("customer");
        final IndexModel customerModel = new IndexModel(customerKeys, customerOptions);

        /*
         * user index 
         */
        final Bson userKeys = Indexes.ascending(Struct.USERS_ACTIONS__USER);
        final IndexOptions userOptions = new IndexOptions().name("user");
        final IndexModel userModel = new IndexModel(userKeys, userOptions);

        /*
         * action index 
         */
        final Bson actionKeys = Indexes.ascending(Struct.USERS_ACTIONS__ACTION);
        final IndexOptions actionOptions = new IndexOptions().name("action");
        final IndexModel actionModel = new IndexModel(actionKeys, actionOptions);

        /*
         * user index 
         */
        final Bson statusKeys = Indexes.ascending(Struct.USERS_ACTIONS__STATUS);
        final IndexOptions statusOptions = new IndexOptions().name("status");
        final IndexModel statusModel = new IndexModel(statusKeys, statusOptions);

        /*
         * text index 
         */
        final Bson textKeys = Indexes.compoundIndex(
                Indexes.text(Struct.USERS_ACTIONS__CUSTOMER),
                Indexes.text(Struct.USERS_ACTIONS__USER),
                Indexes.text(Struct.USERS_ACTIONS__DESCRIPTION),
                Indexes.text(Struct.USERS_ACTIONS__PUBLIC_IP),
                Indexes.text(Struct.USERS_ACTIONS__LOCAL_IP),
                Indexes.text(Struct.USERS_ACTIONS__ERROR)
        );
        final IndexOptions textOptions = new IndexOptions().name("text");
        final IndexModel textModel = new IndexModel(textKeys, textOptions);

        IndexesUtils.createIndexes(mongoConnection, UserAction.class,
                insertionDateModel,
                durationModel,
                customerModel,
                userModel,
                actionModel,
                statusModel,
                textModel);
    }

    /**
     *
     * @param mongoConnection
     * @throws Throwable
     */
    protected void createUserConnectedIndexes(final MongoConnection mongoConnection) throws Throwable {

        /*
         * service index 
         */
        final Bson serviceKeys = Indexes.ascending(Struct.USERS_CONNECTED__SERVICE);
        final IndexOptions serviceOptions = new IndexOptions().name("service");
        final IndexModel serviceModel = new IndexModel(serviceKeys, serviceOptions);

        /*
         * customer index 
         */
        final Bson customerKeys = Indexes.ascending(Struct.USERS_CONNECTED__CUSTOMER);
        final IndexOptions customerOptions = new IndexOptions().name("customer");
        final IndexModel customerModel = new IndexModel(customerKeys, customerOptions);

        /*
         * user index 
         */
        final Bson userKeys = Indexes.ascending(Struct.USERS_CONNECTED__USER);
        final IndexOptions userOptions = new IndexOptions().name("user");
        final IndexModel userModel = new IndexModel(userKeys, userOptions);

        /*
         * serverName index 
         */
        final Bson serverNameKeys = Indexes.ascending(Struct.USERS_CONNECTED__SERVER_NAME);
        final IndexOptions serverNameOptions = new IndexOptions().name("server_name");
        final IndexModel serverNameModel = new IndexModel(serverNameKeys, serverNameOptions);

        IndexesUtils.createIndexes(mongoConnection, UserConnected.class,
                serviceModel,
                customerModel,
                userModel,
                serverNameModel);
    }

    /**
     *
     * @param mongoConnection
     * @throws Throwable
     */
    protected void createCustomerIndexes(final MongoConnection mongoConnection) throws Throwable {

        /*
         * document index 
         */
        final Bson documentKeys = Indexes.ascending(Struct.CUSTOMERS__DOCUMENT);
        final IndexOptions documentOptions = new IndexOptions().name("document");
        final IndexModel documentModel = new IndexModel(documentKeys, documentOptions);

        /*
         * text index 
         */
        final Bson textKeys = Indexes.compoundIndex(
                Indexes.text(Struct.CUSTOMERS__DOCUMENT),
                Indexes.text(Struct.CUSTOMERS__CORPORATE_NAME),
                Indexes.text(Struct.CUSTOMERS__FANCY_NAME),
                Indexes.text(concat(Struct.CUSTOMERS__CONTACT_EMAILS, Struct.CUSTOMERS__CONTACT_EMAILS__EMAIL))
        );
        final IndexOptions textOptions = new IndexOptions().name("text");
        final IndexModel textModel = new IndexModel(textKeys, textOptions);

        IndexesUtils.createIndexes(mongoConnection,
                Customer.class,
                documentModel,
                textModel);
    }
}
