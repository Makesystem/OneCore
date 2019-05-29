/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.mdbi.core.types.ConnectionType;
import com.makesystem.mwi.WebClient;
import com.makesystem.pidgey.io.GetIpHandler;
import com.makesystem.pidgey.io.InnetAddressHelperJRE;
import com.makesystem.pidgey.xml.XmlHelperJRE;
import com.makesystem.xeonentity.core.SystemProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Richeli.vargas
 */
public abstract class OneProperties {

    public static final SystemProperty<String> DATABASE__HOST = new SystemProperty("one__db__host", "127.0.0.1");
    public static final SystemProperty<Integer> DATABASE__PORT = new SystemProperty("one__db__port", 27017);
    public static final SystemProperty<String> DATABASE__NAME = new SystemProperty("one__db__name", "one");
    public static final SystemProperty<String> DATABASE__USER = new SystemProperty("one__db__user", "unknow");
    public static final SystemProperty<String> DATABASE__PASSWORD = new SystemProperty("one__db__password", "unknow");
    public static final SystemProperty<ConnectionType> DATABASE__TYPE = new SystemProperty("one__db__type", ConnectionType.MONGO);
    public static final SystemProperty<String> INNER_HTTP__HOST = new SystemProperty("one__inner_http__host", "127.0.0.1");
    public static final SystemProperty<Integer> INNER_HTTP__PORT = new SystemProperty("one__inner_http__port", 80);
    public static final SystemProperty<Integer> INNER_HTTP__SECURE_PORT = new SystemProperty("one__inner_http__secure_port", 443);
    public static final SystemProperty<Integer> WEBSOCKET_SERVER__TIMEOUT = new SystemProperty("one__websocket_server__timeout", WebClient.SESSION__DEFAULT_TIMEOUT);

    static {

        final boolean hasHttpHost = System.getProperty(INNER_HTTP__HOST.getProperty()) != null;
        final boolean hasHttpPort = System.getProperty(INNER_HTTP__PORT.getProperty()) != null;
        final boolean hasHttpSecurePort = System.getProperty(INNER_HTTP__SECURE_PORT.getProperty()) != null;
        
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

                final String domain_dir = System.getProperty("user.dir");
                final String domain_url = domain_dir + "/domain.xml";
                final Document document = XmlHelperJRE.getDocument(domain_url);

                final Node configs = XmlHelperJRE.getNodeByTag(document, "configs");
                final Node config = XmlHelperJRE.getNodeByTag(configs, "config", "server-config");
                final Node networkConfig = XmlHelperJRE.getNodeByTag(config, "network-config");
                final Node networkListeners = XmlHelperJRE.getNodeByTag(networkConfig, "network-listeners");
                
                final Node networkListener_1 = XmlHelperJRE.getNodeByTag(networkListeners, "network-listener", "http-listener-1");
                final Element networkListener_1_Element = (Element) networkListener_1;
                final String port = networkListener_1_Element.getAttribute("port");

                if (port != null && !port.isEmpty() && !hasHttpPort) {
                    INNER_HTTP__PORT.setValue(Integer.valueOf(port));
                }
                
                final Node networkListener_2 = XmlHelperJRE.getNodeByTag(networkListeners, "network-listener", "http-listener-2");
                final Element networkListener_2_Element = (Element) networkListener_2;
                final String securePort = networkListener_2_Element.getAttribute("port");
                
                if (securePort != null && !securePort.isEmpty() && !hasHttpSecurePort) {
                    INNER_HTTP__SECURE_PORT.setValue(Integer.valueOf(securePort));
                } else if (port != null && !port.isEmpty() && !hasHttpSecurePort) {
                    INNER_HTTP__SECURE_PORT.setValue(Integer.valueOf(port));
                }
                

            } catch (final Throwable throwable) {
                // Ignore
            }

        }
    }

    private OneProperties() {
    }
}
