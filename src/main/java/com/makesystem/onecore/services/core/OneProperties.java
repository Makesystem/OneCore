/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.mdbi.core.types.ConnectionType;
import com.makesystem.mwc.http.server.glasfish.DomainXml;
import static com.makesystem.mwc.http.server.glasfish.DomainXml.getURL;
import com.makesystem.mwc.http.server.glasfish.ServerLog;
import com.makesystem.mwi.WebClient;
import com.makesystem.pidgey.io.GetIpHandler;
import com.makesystem.pidgey.io.InnetAddressHelperJRE;
import com.makesystem.pidgey.lang.ObjectsHelper;
import com.makesystem.pidgey.lang.SystemProperty;
import com.makesystem.pidgey.xml.XmlDocument;
import com.makesystem.pidgey.xml.XmlElement;
import com.makesystem.pidgey.xml.XmlHelper;
import com.makesystem.xeoncore.management.ManagementProperties;
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
    public static final SystemProperty<String> SERVER_NAME = new SystemProperty("one__server_name", "no_name");
    public static final SystemProperty<String> INNER_HTTP__HOST = new SystemProperty("one__inner_http__host", "127.0.0.1");
    public static final SystemProperty<Integer> INNER_HTTP__PORT = new SystemProperty("one__inner_http__port", 80);
    public static final SystemProperty<Integer> INNER_HTTP__SECURE_PORT = new SystemProperty("one__inner_http__secure_port", 443);
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

    private static void loadSystemProperties() {

        final boolean hasHttpHost = System.getProperty(INNER_HTTP__HOST.getProperty()) != null;
        final boolean hasHttpPort = System.getProperty(INNER_HTTP__PORT.getProperty()) != null;
        final boolean hasHttpSecurePort = System.getProperty(INNER_HTTP__SECURE_PORT.getProperty()) != null;
        final boolean hasServerName = System.getProperty(SERVER_NAME.getProperty()) != null;

        if (!hasHttpHost) {
            InnetAddressHelperJRE.getLocalIp(new GetIpHandler() {
                @Override
                public void onSuccess(final String ip) {
                    INNER_HTTP__HOST.setValue(ip);
                }

                @Override
                public void onFailure(final Throwable throwable) {
                }
            });
        }

        if (!hasHttpPort || !hasHttpSecurePort) {

            try {

                final XmlDocument document = DomainXml.getDocument();

                final XmlElement httpListener1 = DomainXml.getHttpListener1(document);
                final String httpListener1_port = httpListener1.getAttribute(DomainXml.Attributes.PORT);

                final XmlElement httpListener2 = DomainXml.getHttpListener1(document);
                final String httpListener2_port = httpListener2.getAttribute(DomainXml.Attributes.PORT);

                if (ObjectsHelper.isNotNull(httpListener1_port)
                        && ObjectsHelper.isNotEmpty(httpListener1_port)) {
                    final Integer value = Integer.valueOf(httpListener1_port);
                    INNER_HTTP__PORT.setValue(value);
                    INNER_HTTP__SECURE_PORT.setValue(value);
                }

                if (ObjectsHelper.isNotNull(httpListener2_port)
                        && ObjectsHelper.isNotEmpty(httpListener2_port)) {
                    final Integer value = Integer.valueOf(httpListener2_port);
                    INNER_HTTP__SECURE_PORT.setValue(value);
                }

            } catch (@SuppressWarnings("UseSpecificCatch") final Throwable ignore) {
                // Ignore
            }

        }

        if (!hasServerName) {
            try {
                final InetAddress addr = InetAddress.getLocalHost();
                SERVER_NAME.setValue(addr.getHostName());
            } catch (@SuppressWarnings("UseSpecificCatch") final Throwable ignore) {
                // Ignore
            }
        }

        new Thread(() -> {
            try {
                Thread.sleep(10000);
                updateDomainXml();
            } catch (@SuppressWarnings("UseSpecificCatch") final Throwable ignore) {
                // Ignore
            }
        }).start();
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
        writeSystemProperty(domain, OneProperties.SERVER_NAME);
        writeSystemProperty(domain, OneProperties.INNER_HTTP__HOST);
        writeSystemProperty(domain, OneProperties.INNER_HTTP__PORT);
        writeSystemProperty(domain, OneProperties.INNER_HTTP__SECURE_PORT);
        writeSystemProperty(domain, OneProperties.WEBSOCKET_SERVER__TIMEOUT);

        // /////////////////////////////////////////////////////////////////////
        // Update Domain.xml
        // /////////////////////////////////////////////////////////////////////
        try {
            //DomainXml.write(domain);
            ServerLog.clear();
            //System.out.println(XmlHelper.toIdentedString(domain.toElement()));
            System.out.println("version: " + domain.getVersion());
            System.out.println("standalone: " + domain.isStandalone());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    protected final static void writeSystemProperty(final XmlDocument domain, final SystemProperty systemProperty) {
        final String value = systemProperty.getValue() == null ? null : systemProperty.getValue().toString();
        DomainXml.setSystemProperty(domain, systemProperty.getProperty(), value);
    }
}
