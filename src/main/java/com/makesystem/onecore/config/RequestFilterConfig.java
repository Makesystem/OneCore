/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.config;

import com.makesystem.mwi.types.MethodType;
import com.makesystem.oneentity.services.OneServices.*;
import com.makesystem.oneentity.services.OneServices.Commons.*;
import com.makesystem.xeoncore.core.AbstractRequestFilter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Richeli.vargas
 */
@WebFilter(urlPatterns = {"/*", ""}, asyncSupported = true)
public class RequestFilterConfig extends AbstractRequestFilter {

    public RequestFilterConfig() {
        registerPublicMethod(PostPing.NAME);
        registerPublicMethod(GetPing.NAME);
        registerPublicMethod(PostEcho.NAME);
        registerPublicMethod(GetEcho.NAME);
    }

    @Override
    protected void doBefore(final ServletRequest servletRequest, final ServletResponse servletResponse) {
    }

    @Override
    protected void doAfter(final ServletRequest servletRequest, final ServletResponse servletResponse) {
    }

    @Override
    protected boolean allowRequest(final ServletRequest servletRequest, final ServletResponse servletResponse) {
        return true;
    }

    @Override
    protected MethodType getMethodType(final ServletRequest servletRequest) {
        return isWebsocketRequest(servletRequest) ? MethodType.ON_OPEN : super.getMethodType(servletRequest);
    }

    protected boolean isWebsocketRequest(final ServletRequest servletRequest) {
        final HttpServletRequest httpServletRequest = ((HttpServletRequest) servletRequest);
        final String requestURI = httpServletRequest.getRequestURI();
        return requestURI.startsWith(Access.CONSUMER);

    }
}
