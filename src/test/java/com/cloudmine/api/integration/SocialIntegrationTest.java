package com.cloudmine.api.integration;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMUser;
import com.cloudmine.api.rest.*;
import com.cloudmine.api.rest.callbacks.LoginResponseCallback;
import com.cloudmine.api.rest.callbacks.SocialGraphCallback;
import com.cloudmine.api.rest.response.LoginResponse;
import com.cloudmine.api.rest.response.SocialGraphResponse;
import com.cloudmine.test.ServiceTestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.cloudmine.test.AsyncTestResultsCoordinator.reset;
import static com.cloudmine.test.AsyncTestResultsCoordinator.waitForTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.apache.commons.io.FileUtils.readFileToByteArray;

/**
 * Created with IntelliJ IDEA.
 * User: ethan
 * Date: 1/18/13
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SocialIntegrationTest extends ServiceTestBase {

    private static final CMUser socialUser = new CMUser("ethan@cloudmine.me", "testing");
    private static UserCMWebService socialService;
    private String gistIdentifier;
    private String gistTestingId;

    @Before
    public void setUp() {
        reset();
        CMApiCredentials.initialize("f2bc46fbb67948cea6dbed4f6f39940f", "c826d8bcad8a4f8685cef78efeb21ec3");

    }

    @Test
    public void testFIRST() {
        socialUser.login( testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                Assert.assertTrue(response.wasSuccess());

                if (response.wasSuccess()) {
                    socialService = CMWebService.getService().setLoggedInUser(socialUser.getSessionToken());
                }
            }
        }));

        waitForTestResults();
        Assert.assertNotNull(socialUser.getSessionToken());
    }

    @Test
    public void testTwitter() {
        reset();
        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.TWITTER,
                HttpVerb.GET,
                "statuses/home_timeline.json",
                new HashMap<String, Object>() {{ put("include_entities", Boolean.valueOf(true)); }},
                null,
                null,
                testCallback(new SocialGraphCallback() {
                    public void onCompletion(SocialGraphResponse response) {
                        assertTrue(response.wasSuccess());
                    }

                    public void onFailure(Throwable t, String msg) {
                        Assert.fail("Failed To Get Timeline!");
                    }
                }
                ));

        waitForTestResults();
    }

    @Ignore //We don't need more tweets. this works.
    @Test
    public void testTweet() {
        reset();

        String tweet = "status=Android Testing! " + UUID.randomUUID().toString();
        ByteArrayEntity tweetEntity = new ByteArrayEntity(tweet.getBytes(Charset.forName("UTF-8")));

        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.TWITTER,
                HttpVerb.POST,
                "statuses/update.json",
                null,
                new HashMap<String, Object>() {{ put("Content-Type", "application/x-www-form-urlencoded"); }},
                tweetEntity,
                testCallback(new SocialGraphCallback() {
                    public void onCompletion(SocialGraphResponse response) {
                        assertTrue(response.wasSuccess());
                    }

                    public void onFailure(Throwable t, String msg) {
                        Assert.fail("Failed To Tweet!");
                    }
                }
                ));


        waitForTestResults();
    }

    @Ignore
    @Test
    public void testCreateGist() {
        gistIdentifier = UUID.randomUUID().toString();

        String gist = "{\"description\":\"Ethan Testing\",\"public\":true,\"files\":{\"" + gistIdentifier + ".txt\":{\"content\":\"" + gistIdentifier + " - String file contents\"}}}";

        reset();

        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.GITHUB,
                HttpVerb.POST,
                "gists",
                null,
                new HashMap<String, Object>() {{ put("Content-Type", "application/json"); }},
                new ByteArrayEntity(gist.getBytes(Charset.forName("UTF-8"))),
                testCallback(new SocialGraphCallback() {
                    public void onCompletion(SocialGraphResponse response) {
                        assertTrue(response.wasSuccess());
                        if (response.wasSuccess()) {
                            try {
                                Map<String, Object> map = JsonUtilities.jsonToMap(response.getMessageBody());
                                gistTestingId = (String)map.get("id");
                                Assert.assertNotNull(gistTestingId);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Assert.fail("Failed to get ID!");
                            }
                        }
                    }

                    public void onFailure(Throwable t, String msg) {
                        Assert.fail("Failed To Make Gist");
                    }
                }
                ));

        waitForTestResults();
    }

    @Ignore
    @Test
    public void testListGist() {
        reset();
        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.GITHUB,
                HttpVerb.GET,
                "gists",
                null,
                null,
                null,
                testCallback(new SocialGraphCallback() {
                    public void onCompletion(SocialGraphResponse response) {
                        assertTrue(response.wasSuccess());
                    }

                    public void onFailure(Throwable t, String msg) {
                        Assert.fail("Failed To List Gist");
                    }
                }
                ));
        waitForTestResults();
    }

    @Ignore
    @Test
    public void testDeleteGist() {
        reset();
        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.GITHUB,
                HttpVerb.DELETE,
                "gists/" + gistTestingId,
                null,
                null,
                null,
                testCallback(new SocialGraphCallback() {
                    public void onCompletion(SocialGraphResponse response) {
                        assertTrue(response.wasSuccess());
                    }

                    public void onFailure(Throwable t, String msg) {
                        Assert.fail("Failed To Delete Gist");
                    }
                }
                ));
        waitForTestResults();
    }

    @Test
    public void testGet404() {
        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.GITHUB,
                HttpVerb.GET,
                "testing404",
                null,
                null,
                null,
                testCallback(new SocialGraphCallback() {

                    public void onCompletion(SocialGraphResponse response) {
                        assertEquals(response.getStatusCode(), 404);
                        assertFalse(response.wasSuccess());
                    }

                    public void onFailure(Throwable t, String msg) {
                        Assert.fail("Failed to make request!" + msg);
                        t.printStackTrace();
                    }
                }
                ));
        waitForTestResults();
    }

    @Test
    public void testDropBoxAccount() {
        reset();
        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.DROPBOX,
                HttpVerb.GET,
                "account/info",
                null,
                new HashMap<String, Object>() {{ put("Content-Type", "application/json"); }},
                null,
                testCallback(new SocialGraphCallback() {
                    public void onCompletion(SocialGraphResponse response) {
                        System.out.println("Code: " + response.getStatusCode());
                        assertTrue(response.wasSuccess());
                    }

                    public void onFailure(Throwable t, String msg) {
                        Assert.fail("Failed To Get Dropbox Account info");
                    }
                }
                ));
        waitForTestResults();
    }

    @Test
    public void testDropboxUpload() {
        try {
            byte[] bytes = readFileToByteArray(new File("/Users/ethan/dropbox_upload_test.txt"));
            ByteArrayEntity file = new ByteArrayEntity(bytes);
            final long length = file.getContentLength();
            System.out.println("Array: " + file.toString() + " - " + file.getContentLength());

            socialService.asyncSocialGraphQueryOnNetwork(
                    CMSocial.Service.DROPBOX,
                    HttpVerb.PUT,
                    "files_put/dropbox/dropbox_upload_test.txt",
                    null,
                    new HashMap<String, Object>() {{ put("Content-Type", "application/octet-stream"); put("Content-Length", "" + length); }},
                    file,
                    testCallback(new SocialGraphCallback() {
                        public void onCompletion(SocialGraphResponse response) {
                            System.out.println("Code2: " + response.getStatusCode());
                            assertTrue(response.wasSuccess());
                        }

                        public void onFailure(Throwable t, String msg) {
                            Assert.fail("Failed To Upload Dropbox File");
                        }
                    }
                    ));
        } catch (Exception e) {
            Assert.fail("Error!");
            e.printStackTrace();
        }
        waitForTestResults();
    }
}
