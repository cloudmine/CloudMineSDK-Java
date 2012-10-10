package com.cloudmine.api.rest;

import com.cloudmine.api.LibrarySpecificClassCreator;
import com.cloudmine.api.Strings;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.CMSocialResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.http.client.methods.HttpGet;

import java.util.*;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSocial {

    public enum Action implements BaseURL {
        SELF("self"), FEED("feed"), FRIENDS("friends"), HOME("home"), HOME_UPDATE("home_Update"), PHOTOS("photos"),
        MENTIONS("mentions"), TIMELINE("timeline"), TWEETS("tweets"), RELATED("related");

        private final String representation;
        private Action(String representation) {
            this.representation = representation;
        }

        @Override
        public String asUrlString() {
            return representation;
        }
    }

    public enum Service implements BaseURL {
        TWITTER("twitter", Action.SELF, Action.FRIENDS, Action.MENTIONS, Action.TIMELINE, Action.TWEETS, Action.RELATED), FACEBOOK("facebook", Action.SELF, Action.FEED, Action.FRIENDS, Action.HOME, Action.HOME_UPDATE, Action.PHOTOS), GITHUB("github");
        private final String representation;
        private final Set<Action> actions;
        private Service(String representation, Action... actions) {
            this.representation = representation;
            this.actions = new HashSet<Action>(Arrays.asList(actions));
        }

        @JsonCreator
        public static Service forValue(String stringRepresentation) {
            return Service.valueOf(stringRepresentation.toUpperCase());
        }

        public Set<Action> getActions() {
            return actions;
        }

        public String asUrlString() {
            return representation;
        }
    }

    private final Map<Service, String> serviceAccessTokenMap = new EnumMap<Service, String>(Service.class);
    private final SinglyURLBuilder baseUrl = new SinglyURLBuilder();
    protected final AsynchronousHttpClient asyncHttpClient;


    public CMSocial() {
        asyncHttpClient = LibrarySpecificClassCreator.getCreator().getAsynchronousHttpClient();
    }

    protected CMSocial(Service service, String key) {
        this();
        storeToken(service, key);
    }


    public void get(Service service, Action action, Callback<CMSocialResponse> callback) {
        String token = serviceAccessTokenMap.get(service);
        if(Strings.isEmpty(token)) {
            callback.onFailure(new IllegalStateException("User must be logged in to access service"), "Missing token for: " + service);
            return;
        }

        HttpGet get = new HttpGet(baseUrl.service(service).action(action).token(token).asUrlString());
        asyncHttpClient.executeCommand(get, callback, CMSocialResponse.CONSTRUCTOR);
    }

    protected void storeToken(Service service, String token) {
        serviceAccessTokenMap.put(service, token);
    }
}
