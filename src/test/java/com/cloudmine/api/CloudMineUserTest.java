package com.cloudmine.api;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/21/12, 12:12 PM
 */
public class CloudMineUserTest {

    @Test
    public void testJson() {
        CMUser user = new CMUser("jake@cloudmine.me", "12345");
        assertEquals("{\"email\":\"jake@cloudmine.me\",\"password\":\"12345\"}", user.asJson());
    }
}
