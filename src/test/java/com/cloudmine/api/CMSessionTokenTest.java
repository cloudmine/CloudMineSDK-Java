package com.cloudmine.api;

import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/21/12, 3:49 PM
 */
public class CMSessionTokenTest {

    @Test
    public void testFromJson() {
        String json = userTokenJson();

        CMSessionToken token = CMSessionToken.CMSessionToken(json);

        assertEquals("44c31131ecac41cf92f49b28b84ebac4", token.getSessionToken());
        Date expectedDate = new Date(1340227389579L);
        assertEquals(expectedDate.toGMTString(), token.getExpiredDate().toGMTString());
    }

    private String userTokenJson() {
        return "{\n" +
                    "    \"session_token\": \"44c31131ecac41cf92f49b28b84ebac4\",\n" +
                    "    \"expires\": \"Wed, 20 Jun 2012 21:23:09 GMT\"\n" +
                    "}";
    }

    @Test
    public void testIsValid() {
        CMSessionToken token = CMSessionToken.CMSessionToken("null");
        assertFalse(token.isValid());
    }
}
