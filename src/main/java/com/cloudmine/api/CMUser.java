package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.callbacks.CMResponseCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.callbacks.CreationResponseCallback;
import com.cloudmine.api.rest.callbacks.LoginResponseCallback;
import com.cloudmine.api.rest.response.CMResponse;
import com.cloudmine.api.rest.response.CreationResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A CMUser consists of an email and a password. When logged in, objects can be specified to be saved
 * at the {@link ObjectLevel.USER}, in which case they must be loaded and saved using the {@link CMSessionToken}
 * obtained by logging in as their associated CMUser. CMUser objects should be instantiated through the static {@link #CMUser(String, String)}
 * function, as platform specific implementations may be necessary.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMUser extends CMObject {
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
        super(false);
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
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void login() throws CreationException {
        login(Callback.DO_NOTHING);
    }

    /**
     * Asynchronously log in this user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link LoginResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.LoginResponseCallback} is passed in
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void login(Callback callback) throws CreationException {
        if(isLoggedIn()) {
            LoginResponse fakeResponse = new LoginResponse(getSessionToken().asJson());
            callback.onCompletion(fakeResponse);
            return;
        }
        CMWebService.getService().asyncLogin(this, setLoggedInUserCallback(callback));
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
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link com.cloudmine.api.rest.response.CreationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CreationResponseCallback} is passed in
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     * @throws JsonConversionException if unable to convert this user to JSON. This should never happen
     */
    public void createUser(Callback callback) throws CreationException, JsonConversionException {
        CMWebService.getService().asyncCreateUser(this, callback);
    }

    /**
     * Equivalent to {@link #createUser(com.cloudmine.api.rest.callbacks.Callback)} with no callback
     */
    public void createUser() throws CreationException, JsonConversionException {
        createUser(Callback.DO_NOTHING);
    }

    /**
     * Asynchronously change this users password
     * @param newPassword the new password
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void changePassword(String newPassword) throws CreationException {
        changePassword(newPassword, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously change this users password
     * @param newPassword the new password
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void changePassword(String newPassword, Callback callback) throws CreationException {
        CMWebService.getService().asyncChangePassword(this, newPassword, callback);
    }

    /**
     * Asynchronously Request that this user's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void resetPasswordRequest() throws CreationException {
        resetPasswordRequest(Callback.DO_NOTHING);
    }

    /**
     * Asynchronously Request that this user's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void resetPasswordRequest(Callback callback) throws CreationException {
        CMWebService.getService().asyncResetPasswordRequest(getEmail(), callback);
    }

    /**
     * Asynchronously confirm that the users password should be reset. Requires the token sent to the user's email address
     * @param emailToken from the e-mail sent to the user
     * @param newPassword the new password
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void resetPasswordConfirmation(String emailToken, String newPassword) throws CreationException {
        resetPasswordConfirmation(emailToken, newPassword, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously confirm that the users password should be reset. Requires the token sent to the user's email address
     * @param emailToken from the e-mail sent to the user
     * @param newPassword the new password
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void resetPasswordConfirmation(String emailToken, String newPassword, Callback callback) throws CreationException {
        CMWebService.getService().asyncResetPasswordConfirmation(emailToken, newPassword, callback);
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

    /**
     * This wraps the given callback in a {@link CreationResponseCallback} that will set this CMUser's object id on
     * success, and then call {@link Callback#onCompletion(Object)} passing in the {@link CreationResponse}
     * You probably don't need to be calling this ever
     * @param callback
     * @return
     */
    public final CreationResponseCallback setObjectIdOnCreation(final Callback callback) {
        return new CreationResponseCallback() {
            public void onCompletion(CreationResponse response) {
                try {
                    if(response.wasSuccess()) {
                        setObjectId(response.getObjectId());
                    }
                } finally {
                    callback.onCompletion(response);
                }
            }
        };
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

    @Override
    public String toString() {
        return email + ":" + password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMUser cmUser = (CMUser) o;

        if (email != null ? !email.equals(cmUser.email) : cmUser.email != null) return false;
        if (password != null ? !password.equals(cmUser.password) : cmUser.password != null) return false;
        if (sessionToken != null ? !sessionToken.equals(cmUser.sessionToken) : cmUser.sessionToken != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = email != null ? email.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (sessionToken != null ? sessionToken.hashCode() : 0);
        return result;
    }
}
