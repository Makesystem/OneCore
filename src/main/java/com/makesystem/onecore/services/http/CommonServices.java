/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.http;

import com.makesystem.mwc.http.server.AbstractServiceServlet;
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
import java.util.concurrent.TimeUnit;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

/**
 *
 * @author Richeli.vargas
 */
@Stateless
@Path(Commons.PATH)
public class CommonServices extends AbstractServiceServlet {

    @POST
    @Path(PostPing.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public void postPing(
            @Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(Boolean.TRUE.toString());
    }

    @GET
    @Path(GetPing.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    public void getPing(
            @Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(Boolean.TRUE.toString());
    }

    @POST
    @Path(PostEcho.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void postEcho(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam(PostEcho.Attributes.DATA) final String data) {
        asyncResponse.resume(data);
    }

    @GET
    @Path(GetEcho.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public void getEcho(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam(GetEcho.Attributes.DATA) final String data) {
        asyncResponse.resume(data);
    }
}
