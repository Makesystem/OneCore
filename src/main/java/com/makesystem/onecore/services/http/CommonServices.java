/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.http;

import com.makesystem.mwc.http.server.AbstractServiceServlet;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Richeli.vargas
 */
@Stateless
@Path("/commons")
public class CommonServices extends AbstractServiceServlet {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/post_ping")
    public String postPing() throws Throwable {
        final StringBuilder builder = new StringBuilder();
            builder.append("ContentType: ").append(httpServletRequest.getContentType()).append(" | ");
            builder.append("ContextPath: ").append(httpServletRequest.getContextPath()).append(" | ");
            builder.append("LocalAddr: ").append(httpServletRequest.getLocalAddr()).append(" | ");
            builder.append("LocalName: ").append(httpServletRequest.getLocalName()).append(" | ");
            builder.append("LocalPort: ").append(httpServletRequest.getLocalPort()).append(" | ");
            builder.append("Method: ").append(httpServletRequest.getMethod()).append(" | ");
            builder.append("RemoteAddr: ").append(httpServletRequest.getRemoteAddr()).append(" | ");
            builder.append("RemoteHost: ").append(httpServletRequest.getRemoteHost()).append(" | ");
            builder.append("RemotePort: ").append(httpServletRequest.getRemotePort()).append(" | ");
            builder.append("RemoteUser: ").append(httpServletRequest.getRemoteUser()).append(" | ");
            builder.append("RequestURI: ").append(httpServletRequest.getRequestURI()).append(" | ");
            builder.append("RequestURL: ").append(httpServletRequest.getRequestURL()).append(" | ");
            builder.append("RequestedSessionId: ").append(httpServletRequest.getRequestedSessionId()).append(" | ");
            builder.append("ServerName: ").append(httpServletRequest.getServerName()).append(" | ");
            builder.append("ServerPort: ").append(httpServletRequest.getServerPort()).append(" | ");
            builder.append("Scheme: ").append(httpServletRequest.getScheme()).append(" | ");
            builder.append("ServletPath: ").append(httpServletRequest.getServletPath());
            
            return builder.toString();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/get_ping")
    public String getPing() throws Throwable {
        return Boolean.TRUE.toString();
    }
}
