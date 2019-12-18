/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.makesystem.onecore.services.core;

import com.makesystem.onecore.services.core.users.UserService;
import com.makesystem.oneentity.services.OneServices;
import com.makesystem.oneentity.services.request.runnable.RequestData;
import com.makesystem.oneentity.services.users.storage.User;
import com.makesystem.pidgey.interfaces.AsyncCallback;
import com.makesystem.pidgey.json.ObjectMapperJRE;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author riche
 */
public class OneRequestService implements Serializable {

    private static final long serialVersionUID = -9074912698818251240L;

    private static final OneRequestService INSTANCE = new OneRequestService();

    public static OneRequestService getInstance() {
        return INSTANCE;
    }

    private OneRequestService() {
    }

    public void request(final OneUser user, final RequestData request, final AsyncCallback<String> callback) throws Throwable {

        switch (user.getService()) {
            case ONE:
                requestOneService(user, request, callback);
                break;
            default:
                throw new IllegalArgumentException("Service not supported: " + user.getService());
                // Do a post request
                //break;
        }

    }

    public void request(final RequestData request, final AsyncCallback<String> callback) throws Throwable {

    }

    public void requestOneService(final OneUser oneUser, final RequestData request, final AsyncCallback<String> callback) throws Throwable {

        switch (request.getMethod()) {

            case OneServices.Users.Insert.CONSUMER: {
                final User user = ObjectMapperJRE.read(request.getParameters().get(OneServices.Users.Insert.Attributes.DATA), User.class);
                final UserService service = UserService.getInstance();
                service.insert(user);
                callback.onSuccess(Boolean.TRUE.toString());
            }
            break;

            case OneServices.Users.Find.CONSUMER: {
                final String filter = request.getParameters().get(OneServices.Users.Find.Attributes.FILTER);
                final UserService service = UserService.getInstance();
                final Collection<User> found = service.find(filter);
                final String response = ObjectMapperJRE.write(found);
                callback.onSuccess(response);
            }
            break;

            default:
                throw new IllegalArgumentException("Unknow method: " + request.getMethod());

        }

    }
}
