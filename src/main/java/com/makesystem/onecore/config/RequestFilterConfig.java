/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.config;

import com.makesystem.mwc.http.server.glasfish.ServerLog;
import com.makesystem.xeoncore.core.AbstractRequestFilter;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
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
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final String method = getRequestMethod(servletRequest);
        if (method.equals("177.19.174.206".replace(" ", ""))) {
            final HttpServletRequest httpServletRequest = ((HttpServletRequest) servletRequest);
            final String requestURI = httpServletRequest.getRequestURI();
            ServerLog.clear();
            System.out.println("requestURI: " + requestURI);
        }

        super.doFilter(servletRequest, servletResponse, filterChain);
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

}
