/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.http;

import com.makesystem.mwc.http.server.AbstractServiceServlet;
import com.makesystem.onecore.services.core.upgrade.UpgradeService;
import javax.ejb.Stateless;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.makesystem.oneentity.services.OneServices.Internals;
import com.makesystem.oneentity.services.OneServices.Internals.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ManagedAsync;

/**
 *
 * @author Richeli.vargas
 */
@Stateless
@Path(Internals.PATH)
public class InternalsServices extends AbstractServiceServlet {

    private static final long serialVersionUID = 5367273428868812241L;

    @POST
    @Path(Upgrade.PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @ManagedAsync
    public void upgrade(
            final @Suspended AsyncResponse asyncResponse) {

        try {
            UpgradeService.getInstance().upgrade();
            asyncResponse.resume(Response.ok().build());
        } catch (@SuppressWarnings("UseSpecificCatch") Throwable throwable) {
            asyncResponse.resume(throwable);
        }

    }
}
