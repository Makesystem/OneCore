/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.xeoncore.core.Version;

/**
 *
 * @author riche
 */
public class OneVersion {

    private static final String APP_NAME = "one";

    private static final String GROUP_ID = "com.makesystem";

    private static final String ARTEFACT_ID = "OneCore";

    private static final Version VERSION = new Version(APP_NAME, GROUP_ID, ARTEFACT_ID);

    public static final String get() {
        return VERSION.get();
    }

}
