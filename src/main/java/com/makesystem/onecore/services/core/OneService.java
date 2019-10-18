/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.oneentity.core.types.DatabaseType;
import com.makesystem.xeoncore.core.AbstractService;
import com.makesystem.xeonentity.core.DatabaseSettings;

/**
 *
 * @author Richeli.vargas
 */
public class OneService extends AbstractService {

    protected static interface OperationAlias {
        // User
        public static final String USER__FIND_BY__LOGIN_AND_PASSWORD    = "USER__FIND_BY__LOGIN_AND_PASSWORD";
        public static final String USER__INSERT                         = "USER__INSERT";
        // Connected User
        public static final String CONNECTED_USER__INSERT               = "CONNECTED_USER__INSERT";
        public static final String CONNECTED_USER__DELETE               = "CONNECTED_USER__DELETE";
        public static final String CONNECTED_USER__CLEAR                = "CONNECTED_USER__CLEAR";        
        public static final String CONNECTED_USER__FIND                 = "CONNECTED_USER__LIST";       
        public static final String CONNECTED_USER__COUNT                = "CONNECTED_USER__COUNT";
        // User Action
        public static final String USER_ACTION__INSERT                  = "USER_ACTION__INSERT";
        public static final String USER_ACTION__UPDATE                  = "USER_ACTION__UPDATE";
    }
    
    public OneService() {
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

}
