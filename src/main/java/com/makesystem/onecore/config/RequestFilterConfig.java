/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.config;

import com.makesystem.oneentity.services.OneServices.Commons.*;
import com.makesystem.oneentity.services.OneServices.Internals.*;
import com.makesystem.xeoncore.core.AbstractRequestFilter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 *
 * @author Richeli.vargas
 */
@WebFilter(urlPatterns = {"/*", ""}, asyncSupported = true)
public class RequestFilterConfig extends AbstractRequestFilter {

    private static final long serialVersionUID = -916802795208061995L;

    public RequestFilterConfig() {                
        registerPublicMethod(Version.NAME);
        registerPublicMethod(PostPing.NAME);
        registerPublicMethod(GetPing.NAME);
        registerPublicMethod(PostEcho.NAME);
        registerPublicMethod(GetEcho.NAME);
        /* For tests */
        /* IT MUST BE REMOVED */
        registerPublicMethod(Upgrade.NAME);
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
