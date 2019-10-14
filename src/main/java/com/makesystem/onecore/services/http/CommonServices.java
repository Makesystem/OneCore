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
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;

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
            final @Suspended AsyncResponse asyncResponse) {
        (new Thread(() -> asyncResponse.resume(Response.ok(Boolean.TRUE.toString()).build()))).start();
    }

    @GET
    @Path(GetPing.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    public void getPing(
            final @Suspended AsyncResponse asyncResponse) {
        (new Thread(() -> asyncResponse.resume(Response.ok(Boolean.TRUE.toString()).build()))).start();
    }

    @POST
    @Path(PostEcho.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void postEcho(
            final @Suspended AsyncResponse asyncResponse,
            @FormParam(PostEcho.Attributes.DATA) final String data) {
        (new Thread(() -> asyncResponse.resume(Response.ok(data).build()))).start();
    }

    @GET
    @Path(GetEcho.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public void getEcho(
            final @Suspended AsyncResponse asyncResponse,
            @QueryParam(GetEcho.Attributes.DATA) final String data) {
        (new Thread(() -> asyncResponse.resume(Response.ok(data).build()))).start();
    }
}
