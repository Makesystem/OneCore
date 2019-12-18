/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.oneentity.core.types.DatabaseType;
import com.makesystem.xeoncore.core.DefaultService;
import com.makesystem.xeonentity.core.DatabaseSettings;

/**
 *
 * @author Richeli.vargas
 */
public class OneService extends DefaultService {

    private static final long serialVersionUID = 7694509191783532177L;

    protected static interface OperationAlias {
        // User
        public static final String USER__FIND_BY__LOGIN_AND_PASSWORD    = "USER__FIND_BY__LOGIN_AND_PASSWORD";
        public static final String USER__IS_DOCUMENT_AVALIABLE          = "USER__IS_DOCUMENT_AVALIABLE";
        public static final String USER__IS_LOGIN_AVALIABLE             = "USER__IS_LOGIN_AVALIABLE";
        public static final String USER__IS_EMAIL_AVALIABLE             = "USER__IS_EMAIL_AVALIABLE";
        public static final String USER__INSERT                         = "USER__INSERT";
        public static final String USER__FIND                           = "USER__FIND";
        // Connected User
        public static final String USER_CONNECTED__INSERT               = "USER_CONNECTED__INSERT";
        public static final String USER_CONNECTED__DELETE               = "USER_CONNECTED__DELETE";
        public static final String USER_CONNECTED__CLEAR                = "USER_CONNECTED__CLEAR";        
        public static final String USER_CONNECTED__FIND                 = "USER_CONNECTED__LIST";       
        public static final String USER_CONNECTED__COUNT                = "USER_CONNECTED__COUNT";
        // User Action
        public static final String USER_ACTION__INSERT                  = "USER_ACTION__INSERT";
        public static final String USER_ACTION__UPDATE                  = "USER_ACTION__UPDATE";
        // User
        public static final String CUSTOMER__IS_DOCUMENT_AVALIABLE      = "CUSTOMER__IS_DOCUMENT_AVALIABLE";
        public static final String CUSTOMER__INSERT                     = "CUSTOMER__INSERT";
        public static final String CUSTOMER__FIND                       = "CUSTOMER__FIND";
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
