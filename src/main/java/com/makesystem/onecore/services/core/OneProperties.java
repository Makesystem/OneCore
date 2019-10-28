/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.mdbi.core.types.ConnectionType;
import com.makesystem.mwc.http.server.glasfish.DomainXml;
import com.makesystem.mwi.WebClient;
import com.makesystem.pidgey.io.net.IpAddress;
import com.makesystem.pidgey.io.net.IpAddressJRE;
import com.makesystem.pidgey.lang.ObjectHelper;
import com.makesystem.pidgey.lang.SystemProperty;
import com.makesystem.pidgey.xml.XmlDocument;
import com.makesystem.pidgey.xml.XmlElement;
import com.makesystem.xeoncore.management.ManagementProperties;
import java.io.IOException;
import java.net.InetAddress;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 *
 * @author Richeli.vargas
 */
@WebListener
public final class OneProperties implements ServletContextListener {

    public static final SystemProperty<String> DATABASE__HOST = new SystemProperty("one__db__host", "127.0.0.1");
    public static final SystemProperty<Integer> DATABASE__PORT = new SystemProperty("one__db__port", 27017);
    public static final SystemProperty<String> DATABASE__NAME = new SystemProperty("one__db__name", "one");
    public static final SystemProperty<String> DATABASE__USER = new SystemProperty("one__db__user", "unknow");
    public static final SystemProperty<String> DATABASE__PASSWORD = new SystemProperty("one__db__password", "unknow");
    public static final SystemProperty<ConnectionType> DATABASE__TYPE = new SystemProperty("one__db__type", ConnectionType.MONGO);
    public static final SystemProperty<Integer> DATABASE__POOL_SIZE = new SystemProperty("one__db__pool_size", 100);
    public static final SystemProperty<String> SERVER_NAME = new SystemProperty("one__server_name", "no_name");    
    public static final SystemProperty<Integer> WEBSOCKET_SERVER__TIMEOUT = new SystemProperty("one__websocket_server__timeout", WebClient.SESSION__DEFAULT_TIMEOUT);

    public OneProperties() {
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        loadSystemProperties();
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void contextDestroyed(final ServletContextEvent event) {
        // Nothing
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void loadSystemProperties() {

        final boolean hasServerName = System.getProperty(SERVER_NAME.getProperty()) != null;

        if (!hasServerName) {
            try {
                final InetAddress addr = InetAddress.getLocalHost();
                SERVER_NAME.setValue(addr.getHostName());
            } catch (@SuppressWarnings("UseSpecificCatch") final Throwable ignore) {
                // Ignore
                ignore.printStackTrace();
            }
        }

        callUpdateDomainXml();        

    }

    @SuppressWarnings("CallToPrintStackTrace")
    public final static void callUpdateDomainXml() {
        try {
            updateDomainXml();
        } catch (@SuppressWarnings("UseSpecificCatch") final Throwable ignore) {
            // Ignore
            ignore.printStackTrace();
        }
    }

    public final static void updateDomainXml() throws Throwable {

        // /////////////////////////////////////////////////////////////////////
        // Get Domain.xml
        // /////////////////////////////////////////////////////////////////////
        final XmlDocument domain = DomainXml.getDocument();

        // /////////////////////////////////////////////////////////////////////
        // Management Properties
        // /////////////////////////////////////////////////////////////////////
        writeSystemProperty(domain, ManagementProperties.DATABASE__HOST);
        writeSystemProperty(domain, ManagementProperties.DATABASE__NAME);
        writeSystemProperty(domain, ManagementProperties.DATABASE__PASSWORD);
        writeSystemProperty(domain, ManagementProperties.DATABASE__PORT);
        writeSystemProperty(domain, ManagementProperties.DATABASE__TYPE);
        writeSystemProperty(domain, ManagementProperties.DATABASE__USER);

        // /////////////////////////////////////////////////////////////////////
        // One Properties
        // /////////////////////////////////////////////////////////////////////
        writeSystemProperty(domain, OneProperties.DATABASE__HOST);
        writeSystemProperty(domain, OneProperties.DATABASE__PORT);
        writeSystemProperty(domain, OneProperties.DATABASE__NAME);
        writeSystemProperty(domain, OneProperties.DATABASE__USER);
        writeSystemProperty(domain, OneProperties.DATABASE__PASSWORD);
        writeSystemProperty(domain, OneProperties.DATABASE__TYPE);
        writeSystemProperty(domain, OneProperties.DATABASE__POOL_SIZE);
        writeSystemProperty(domain, OneProperties.SERVER_NAME);
        writeSystemProperty(domain, OneProperties.WEBSOCKET_SERVER__TIMEOUT);

        // /////////////////////////////////////////////////////////////////////
        // Update Domain.xml
        // /////////////////////////////////////////////////////////////////////
        DomainXml.write(domain);
    }

    protected final static void writeSystemProperty(final XmlDocument domain, final SystemProperty systemProperty) {
        final String value = systemProperty.getValue() == null ? null : systemProperty.getValue().toString();
        DomainXml.setSystemProperty(domain, systemProperty.getProperty(), value);
    }
}
