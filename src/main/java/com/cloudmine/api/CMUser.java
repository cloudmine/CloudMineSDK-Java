package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.FakeFuture;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.callbacks.CMResponseCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.callbacks.LoginResponseCallback;
import com.cloudmine.api.rest.response.CMResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * A CMUser consists of an email and a password. When logged in, objects can be specified to be saved
 * at the {@link ObjectLevel.USER}, in which case they must be loaded and saved using the {@link CMSessionToken}
 * obtained by logging in as their associated CMUser. CMUser objects should be instantiated through the static {@link #CMUser(String, String)}
 * function, as platform specific implementations may be necessary.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMUser {
    private static final Logger LOG = LoggerFactory.getLogger(CMUser.class);

    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";

    private final String email;
    private final String password;
    private CMSessionToken sessionToken;
    /**
     * Instantiate a new CMUser instance with the given email and password
     * @param email email of the user
     * @param password password for the user
     * @return a new CMUser instance
     * @throws CreationException if email or password are null
     */
    public static CMUser CMUser(String email, String password) throws CreationException {
        return new AndroidCMUser(email, password);
    }

    /**
     * Don't call this, use the static constructor instead
     * @param email
     * @param password
     * @throws CreationException
     */
    CMUser(String email, String password) throws CreationException {
        if(email == null) {
            throw new CreationException("User cannot have null email");
        }
        if(password == null) {
            throw new CreationException("User cannot have null password");
        }
        this.email = email;
        this.password = password;
    }

    public String asJson() throws JsonConversionException {
        Map<String, Object> jsonMap = new HashMap<String, Object>(); //TODO switch this to a more manual process to reduce number of objects created
        jsonMap.put(EMAIL_KEY, email);
        jsonMap.put(PASSWORD_KEY, password);
        return JsonUtilities.mapToJson(jsonMap);
    }

    /**
     * The users email address
     * @return The users email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * The users password
     * @return The users password
     */
    public String getPassword() {
        return password;
    }

    public CMSessionToken getSessionToken() {
        if(sessionToken == null) {
            return CMSessionToken.FAILED;
        }
        return sessionToken;
    }

    /**
     * Check whether this user is logged in
     * @return true if the user is logged in successfully; false otherwise
     */
    public boolean isLoggedIn() {
        return sessionToken != null && sessionToken.isValid();
    }

    /**
     * Asynchronously log in this user
     * @return A Future containing the {@link LoginResponse} which will include the CMSessionToken that authenticates this user and provides access to the user level store
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public Future<LoginResponse> login() throws CreationException {
        return login(Callback.DO_NOTHING);
    }

    /**
     * Asynchronously log in this user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link LoginResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.LoginResponseCallback} is passed in
     * @return A Future containing the {@link LoginResponse} which will include the CMSessionToken that authenticates this user and provides access to the user level store
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public Future<LoginResponse> login(Callback callback) throws CreationException {
        if(isLoggedIn()) {
            LoginResponse fakeResponse = new LoginResponse(getSessionToken().asJson());
            callback.onCompletion(fakeResponse);
            return new FakeFuture<LoginResponse>(fakeResponse);
        }
        return CMWebService.getService().asyncLogin(this, setLoggedInUserCallback(callback));
    }

    /**
     * Asynchronously log out this user
     */
    public void logout() {
        logout(Callback.DO_NOTHING);
    }

    /**
     * Asynchronously log out this user
     * @param callback a {@link Callback} that expects a {@link CMResponse}. It is recommended that a {@link CMResponseCallback} is used here
     */
    public void logout(Callback callback) {
        CMWebService.getService().asyncLogout(getSessionToken(), setLoggedOutUserCallback(callback));
    }

    /**
     * Asynchronously create this user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @return A Future containing the {@link LoginResponse} which will include the CMSessionToken that authenticates this user and provides access to the user level store
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     * @throws JsonConversionException if unable to convert this user to JSON. This should never happen
     */
    public Future<CMResponse> createUser(Callback callback) throws CreationException, JsonConversionException {
        return CMWebService.getService().asyncCreateUser(this, callback);
    }

    /**
     * Asynchronously create this user
     * @return A Future containing the {@link LoginResponse} which will include the CMSessionToken that authenticates this user and provides access to the user level store
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     * @throws JsonConversionException if unable to convert this user to JSON. This should never happen
     */
    public Future<CMResponse> createUser() throws CreationException, JsonConversionException {
        return createUser(Callback.DO_NOTHING);
    }

    /**
     * Asynchronously change this users password
     * @param newPassword the new password
     * @return a Future containing the {@link CMResponse} generated by this request
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public Future<CMResponse> changePassword(String newPassword) throws CreationException {
        return changePassword(newPassword, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously change this users password
     * @param newPassword the new password
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @return a Future containing the {@link CMResponse} generated by this request
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public Future<CMResponse> changePassword(String newPassword, Callback callback) throws CreationException {
        return CMWebService.getService().asyncChangePassword(this, newPassword, callback);
    }

    /**
     * Asynchronously Request that this user's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @return a Future containing the {@link CMResponse} generated by this request
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public Future<CMResponse> resetPasswordRequest() throws CreationException {
        return resetPasswordRequest(Callback.DO_NOTHING);
    }

    /**
     * Asynchronously Request that this user's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @return a Future containing the {@link CMResponse} generated by this request
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public Future<CMResponse> resetPasswordRequest(Callback callback) throws CreationException {
        return CMWebService.getService().asyncResetPasswordRequest(getEmail(), callback);
    }

    /**
     * Asynchronously confirm that the users password should be reset. Requires the token sent to the user's email address
     * @param emailToken from the e-mail sent to the user
     * @param newPassword the new password
     * @return a Future containing the {@link CMResponse} generated by this request
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public Future<CMResponse> resetPasswordConfirmation(String emailToken, String newPassword) throws CreationException {
        return resetPasswordConfirmation(emailToken, newPassword, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously confirm that the users password should be reset. Requires the token sent to the user's email address
     * @param emailToken from the e-mail sent to the user
     * @param newPassword the new password
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @return a Future containing the {@link CMResponse} generated by this request
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public Future<CMResponse> resetPasswordConfirmation(String emailToken, String newPassword, Callback callback) throws CreationException {
        return CMWebService.getService().asyncResetPasswordConfirmation(emailToken, newPassword, callback);
    }

    /**
     * Encode this CMUser's email and password as a Base64 string. The format is email:password
     * @return a Base64 representation of this user
     */
    public String encode() {
        String userString = email + ":" + password;
        String encodedString = encodeString(userString);
        return encodedString;
    }

    protected String encodeString(String toEncode) {
        try {
            return null; //This is cool cause we're always returning AndroidCMUser
//            return javax.xml.bind.DatatypeConverter.printBase64Binary(toEncode.getBytes());
        }catch(NoClassDefFoundError ncdfe) {
            LOG.error("Do not instantiate CMUser objects on Android! You must use AndroidCMUser, as " +
                    "android does not provide an implementation for DataTypeConverter", ncdfe);
            throw ncdfe;
        }
    }

    @Override
    public String toString() {
        return email + ":" + password;
    }


    private final LoginResponseCallback setLoggedInUserCallback(final Callback callback) {
        return new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                try {
                    if(response.wasSuccess() &&
                            response.getSessionToken().isValid()) {
                        sessionToken = response.getSessionToken();
                    }
                }finally {
                    callback.onCompletion(response);
                }
            }
        };
    }

    private final CMResponseCallback setLoggedOutUserCallback(final Callback callback) {
        return new CMResponseCallback() {
            public void onCompletion(CMResponse response) {
                try {
                    if(response.wasSuccess()) {
                        sessionToken = null;
                    }
                } finally {
                    callback.onCompletion(response);
                }
            }
        };
    }
}
