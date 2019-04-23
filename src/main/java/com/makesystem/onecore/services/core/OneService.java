/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.xeoncore.core.AbstractService;
import com.makesystem.xeonentity.core.DatabaseSettings;
import com.makesystem.xeonentity.core.types.DatabaseType;

/**
 *
 * @author Richeli.vargas
 */
public class OneService extends AbstractService {

    protected static interface OperationAlias {
        public static final String USER__FIND_BY__LOGIN_AND_PASSWORD = "USER__FIND_BY__LOGIN_AND_PASSWORD";
        public static final String USER__INSERT = "USER__INSERT";
    }
    
    public OneService() {
        super(new DatabaseSettings(
                OneProperties.DATABASE__HOST.getValue(),
                OneProperties.DATABASE__PORT.getValue(),
                OneProperties.DATABASE__NAME.getValue(),
                OneProperties.DATABASE__USER.getValue(),
                OneProperties.DATABASE__PASSWORD.getValue(),
                OneProperties.DATABASE__TYPE.getValue(),
                DatabaseType.ONE));
    }

}
