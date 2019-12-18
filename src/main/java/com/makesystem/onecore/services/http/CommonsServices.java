/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.http;

import com.makesystem.mwc.http.server.AbstractServiceServlet;
import com.makesystem.mwc.util.helper.ServletHelper;
import com.makesystem.onecore.services.core.OneVersion;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.makesystem.oneentity.services.OneServices.Commons;
import com.makesystem.oneentity.services.OneServices.Commons.*;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ManagedAsync;

/**
 *
 * @author Richeli.vargas
 */
@Stateless
@Path(Commons.PATH)
public class CommonsServices extends AbstractServiceServlet {

    private static final long serialVersionUID = 5010490610209269871L;

    @GET
    @Path(Version.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @ManagedAsync
    public void version(
            final @Suspended AsyncResponse asyncResponse) {
        asyncResponse.resume(Response.ok(OneVersion.get()).build());
    }
    
    @GET
    @Path(GetIp.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @ManagedAsync
    public void getIp(
            final @Suspended AsyncResponse asyncResponse) {
        asyncResponse.resume(Response.ok(ServletHelper.getRemoteHost(httpServletRequest)).build());
    }
    
    @POST
    @Path(PostPing.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @ManagedAsync
    public void postPing(
            final @Suspended AsyncResponse asyncResponse) {
        asyncResponse.resume(Response.ok(Boolean.TRUE.toString()).build());
    }

    @GET
    @Path(GetPing.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @ManagedAsync
    public void getPing(
            final @Suspended AsyncResponse asyncResponse) {
        asyncResponse.resume(Response.ok(Boolean.TRUE.toString()).build());
    }

    @POST
    @Path(PostEcho.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ManagedAsync
    public void postEcho(
            final @Suspended AsyncResponse asyncResponse,
            @FormParam(PostEcho.Attributes.DATA) final String data) {
        asyncResponse.resume(Response.ok(data).build());
    }

    @GET
    @Path(GetEcho.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @ManagedAsync
    public void getEcho(
            final @Suspended AsyncResponse asyncResponse,
            @QueryParam(GetEcho.Attributes.DATA) final String data) {
        asyncResponse.resume(Response.ok(data).build());
    }
}
