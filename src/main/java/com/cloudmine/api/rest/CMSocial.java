package com.cloudmine.api.rest;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Provides access to CMSocial functionality
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSocial {

    public enum Service implements BaseURL {
        TWITTER("twitter", "https://api.twitter.com"),
        FACEBOOK("facebook", "https://graph.facebook.com"),
        GITHUB("github", "https://github.com"),
        FOURSQUARE("foursquare", "https://foursquare.com"),
        FITBIT("fitbit", "http://www.fitbit.com/"),
        INSTAGRAM("instagram", "https://instagram.com"),
        TUMBLR("tumblr", "https://www.tumblr.com"),
        LINKEDIN("linkedin", "https://www.linkedin.com"),
        DROPBOX("dropbox", "https://www.dropbox.com"),
        MEETUP("meetup", "https://secure.meetup.com"),
        RUNKEEPER("runkeeper", "https://runkeeper.com"),
        WITHINGS("withings", "http://auth.withings.com/"),
        WORDPRESS("wordpress", "https://public-api.wordpress.com"),
        YAMMER("yammer", "https://www.yammer.com"),
        FLICKR("flickr", "https://login.yahoo.com");
        private final String representation;
        private final String authenticationUrl;
        private Service(String representation, String authURL) {
            this.representation = representation;
            this.authenticationUrl = authURL;
        }

        public String getAuthenticationUrl() {
            return authenticationUrl;
        }

        @JsonCreator
        public static Service forValue(String stringRepresentation) {
            return Service.valueOf(stringRepresentation.toUpperCase());
        }

        public String asUrlString() {
            return representation;
        }
    }
}
