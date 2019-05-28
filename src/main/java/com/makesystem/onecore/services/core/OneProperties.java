/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.mdbi.core.types.ConnectionType;
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

    static {

        if (System.getProperty(INNER_HTTP__HOST.getProperty()) == null) {
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

        if (System.getProperty(INNER_HTTP__PORT.getProperty()) == null) {

            try {

                final String domain_dir = System.getProperty("user.dir");
                final String domain_url = domain_dir + "/domain.xml";
                final Document document = XmlHelperJRE.getDocument(domain_url);

                final Node configs = XmlHelperJRE.getNodeByTag(document, "configs");
                final Node config = XmlHelperJRE.getNodeByTag(configs, "config", "server-config");
                final Node networkConfig = XmlHelperJRE.getNodeByTag(config, "network-config");
                final Node networkListeners = XmlHelperJRE.getNodeByTag(networkConfig, "network-listeners");
                final Node networkListener = XmlHelperJRE.getNodeByTag(networkListeners, "network-listener", "http-listener-1");

                final Element networkListenerElement = (Element) networkListener;
                final String port = networkListenerElement.getAttribute("port");

                if (port != null && !port.isEmpty()) {
                    INNER_HTTP__PORT.setValue(Integer.valueOf(port));
                }

            } catch (final Throwable throwable) {
                // Ignore
            }

        }
    }

    private OneProperties() {
    }
}
