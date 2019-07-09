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
import com.makesystem.oneentity.services.OneHttpServices.Commons;
import com.makesystem.oneentity.services.OneHttpServices.Commons.*;
import javax.ws.rs.PathParam;

/**
 *
 * @author Richeli.vargas
 */
@Stateless
@Path(Commons.PATH)
public class CommonServices extends AbstractServiceServlet {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path(PostPing.PATH)
    public String postPing() throws Throwable {
        return Boolean.TRUE.toString();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path(GetPing.PATH)
    public String getPing() throws Throwable {
        return Boolean.TRUE.toString();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path(PostEcho.PATH)
    public String postEcho(@FormParam(PostEcho.Attributes.DATA) final String data) throws Throwable {
        return data;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path(GetEcho.PATH)
    public String getEcho(@PathParam(GetEcho.Attributes.DATA) final String data) throws Throwable {
        return data;
    }
}
