/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.config;

import com.makesystem.oneentity.services.OneServices;
import com.makesystem.xeoncore.core.AbstractRequestFilter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Richeli.vargas
 */
@WebFilter(urlPatterns = {"/*", ""})
public class RequestFilterConfig extends AbstractRequestFilter {

    public RequestFilterConfig() {
        registerPublicMethod("post_ping");
        registerPublicMethod("get_ping");
        registerPublicMethod("post_echo");
        registerPublicMethod("get_echo");
    }

    @Override
    protected void doBefore(final ServletRequest servletRequest, final ServletResponse servletResponse) {
    }

    @Override
    protected void doAfter(final ServletRequest servletRequest, final ServletResponse servletResponse) {
    }

    @Override
    protected boolean allowRequest(final ServletRequest servletRequest, final ServletResponse servletResponse) {
        
        final HttpServletRequest httpServletRequest = ((HttpServletRequest) servletRequest);
        final String requestURI = httpServletRequest.getRequestURI();
        
        final boolean isWebsocketRequest = requestURI.startsWith(OneServices.Access.CONSUMER);
        
        return isWebsocketRequest || true;
    }

}
