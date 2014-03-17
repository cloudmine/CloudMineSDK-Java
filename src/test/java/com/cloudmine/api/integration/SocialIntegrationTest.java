package com.cloudmine.api.integration;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.JavaCMUser;
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
import java.nio.charset.UnsupportedCharsetException;
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

    private static final JavaCMUser socialUser = new JavaCMUser("ethan@cloudmine.me", "testing");
    private static UserCMWebService socialService;
    private String gistIdentifier;
    private String gistTestingId;

    @Test
    public void testFIRST() {

        socialUser.login( testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {

                if (response.wasSuccess()) {
                    socialService = CMWebService.getService().setLoggedInUser(socialUser.getSessionToken());
                    System.out.println("Authenticated: " + socialUser.getAuthenticatedServices());
                }
                Assert.assertTrue(response.wasSuccess());
            }

            public void onFailure(Throwable t, String msg) {
                t.printStackTrace();
            }
        }));

        waitForTestResults();
        Assert.assertNotNull(socialUser.getSessionToken());
        Assert.assertFalse(socialUser.getSessionToken().toString().equalsIgnoreCase("invalidToken"));
    }

    @Test
    public void testTwitter() {
        System.out.println("Service: " + socialService);

        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.TWITTER,
                HttpVerb.GET,
                "statuses/home_timeline.json",
                new HashMap<String, Object>() {{ put("include_entities", Boolean.valueOf(true)); }},
                null,
                null,
                testCallback(new SocialGraphCallback() {
                    public void onCompletion(SocialGraphResponse response) {
                        System.out.println("Body: " + response.getMessageBody() + " - " + response.getStatusCode());
                        System.out.println("Test? " + response.wasSuccess());
                        assertTrue(response.wasSuccess());
                    }
                }
                ));

        waitForTestResults();
    }

    @Test
    public void testTweet() {

        String tweet = "status=Android Testing! " + UUID.randomUUID().toString();
        ByteArrayEntity tweetEntity = getByteArrayEntity(tweet);

        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.TWITTER,
                HttpVerb.POST,
                "statuses/update.json",
                null,
                new HashMap<String, Object>() {{ put("Content-Type", "application/x-www-form-urlencoded"); }},
                tweetEntity,
                testCallback(new SocialGraphCallback() {
                    public void onCompletion(SocialGraphResponse response) {
                        System.out.print("Body: " + response.getMessageBody() + " - " + response.getStatusCode());
                        assertTrue(response.wasSuccess());
                    }
                }
                ));


        waitForTestResults();
    }

    private ByteArrayEntity getByteArrayEntity(String entity) {
        ByteArrayEntity tweetEntity = null;
        try {
            tweetEntity = new ByteArrayEntity(entity.getBytes(Charset.forName("UTF-8").toString()));
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException("UTF-8");
        }
        return tweetEntity;
    }

    @Test
    public void testCreateGist() {
        gistIdentifier = UUID.randomUUID().toString();

        String gist = "{\"description\":\"Ethan Testing\",\"public\":true,\"files\":{\"" + gistIdentifier + ".txt\":{\"content\":\"" + gistIdentifier + " - String file contents\"}}}";

        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.GITHUB,
                HttpVerb.POST,
                "gists",
                null,
                new HashMap<String, Object>() {{ put("Content-Type", "application/json"); }},
                getByteArrayEntity(gist),
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
                }
                ));

        waitForTestResults();
    }

    @Test
    public void testListGist() {
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
                }
                ));
        waitForTestResults();
    }

    @Test
    public void testDeleteGist() {
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
                }
                ));
        waitForTestResults();
    }

    @Test
    public void testDropBoxAccount() {
        socialService.asyncSocialGraphQueryOnNetwork(
                CMSocial.Service.DROPBOX,
                HttpVerb.GET,
                "account/info",
                null,
                new HashMap<String, Object>() {{ put("Content-Type", "application/json"); }},
                null,
                testCallback(new SocialGraphCallback() {
                    public void onCompletion(SocialGraphResponse response) {
                        assertTrue(response.wasSuccess());
                    }
                }
                ));
        waitForTestResults();
    }

    @Test
    public void testDropboxUpload() {
        try {
            ///
            /// Create File
            ///
            PrintWriter writer = new PrintWriter("dropbox_upload_test.txt", "UTF-8");
            writer.println("This is a test!");
            writer.close();

            byte[] bytes = readFileToByteArray(new File("dropbox_upload_test.txt"));
            ByteArrayEntity file = new ByteArrayEntity(bytes);
            final long length = file.getContentLength();

            socialService.asyncSocialGraphQueryOnNetwork(
                    CMSocial.Service.DROPBOX,
                    HttpVerb.PUT,
                    "files_put/dropbox/dropbox_upload_test.txt",
                    null,
                    new HashMap<String, Object>() {{ put("Content-Type", "application/octet-stream"); put("Content-Length", "" + length); }},
                    file,
                    testCallback(new SocialGraphCallback() {
                        public void onCompletion(SocialGraphResponse response) {
                            assertTrue(response.wasSuccess());
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
