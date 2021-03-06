/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.config;

import com.makesystem.onecore.services.http.CommonsServices;
import com.makesystem.onecore.services.http.InternalsServices;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import javax.ws.rs.core.Application;

/**
 *
 * @author Richeli.vargas
 */
public class ApplicationConfig extends Application implements Serializable {

    private static final long serialVersionUID = -5219916371443251537L;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));        
    }

    @Override
    public Set<Class<?>> getClasses() {

        final Set<Class<?>> resources = new HashSet<>();
        resources.add(CommonsServices.class);
        resources.add(InternalsServices.class);

        return resources;
    }
}
