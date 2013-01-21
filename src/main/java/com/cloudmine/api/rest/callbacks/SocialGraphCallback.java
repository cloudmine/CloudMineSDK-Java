package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.SocialGraphResponse;

/**
 * Created with IntelliJ IDEA.
 * User: ethan
 * Date: 1/18/13
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class SocialGraphCallback extends CMCallback<SocialGraphResponse> {
    public SocialGraphCallback() {
        super(SocialGraphResponse.CONSTRUCTOR);
    }
}
