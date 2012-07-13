package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.persistance.CloudMineObject;
import com.cloudmine.api.rest.Base64Encoder;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.callbacks.*;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.CMResponse;
import com.cloudmine.api.rest.response.CreationResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A CMUser consists of an email and a password. When logged in, objects can be specified to be saved
 * at the {@link ObjectLevel.USER}, in which case they must be loaded and saved using the {@link CMSessionToken}
 * obtained by logging in as their associated CMUser. CMUser objects should be instantiated through the static {@link #CMUser(String, String)}
 * function, as platform specific implementations may be necessary.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
@CloudMineObject
public class CMUser extends CMObject {
    private static final Logger LOG = LoggerFactory.getLogger(CMUser.class);

    public static final String MISSING_VALUE = "unset";
    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";
    public static final String CREDENTIALS_KEY = "credentials";
    public static final String PROFILE_KEY = "profile";

    private String email;
    private String password;
    private CMSessionToken sessionToken;
    /**
     * Instantiate a new CMUser instance with the given email and password
     * @param email email of the user
     * @param password password for the user
     * @return a new CMUser instance
     * @throws CreationException if email or password are null
     */
    public static CMUser CMUser(String email, String password) throws CreationException {
        return new CMUser(email, password);
    }


    protected CMUser() {
        this("unset", "unset");
    }
    /**
     * Don't call this, use the static constructor instead
     * @param email
     * @param password
     * @throws CreationException
     */
    protected CMUser(String email, String password) throws CreationException {
        super(false);
        this.email = email;
        this.password = password;
    }

    public String asJson() throws JsonConversionException {
        String credentialsJson = JsonUtilities.jsonCollection(
                                        JsonUtilities.createJsonProperty(EMAIL_KEY, getEmail()),
                                        JsonUtilities.createJsonProperty(PASSWORD_KEY, getPassword())).asJson();
        return JsonUtilities.jsonCollection(
                JsonUtilities.createJsonPropertyToJson(CREDENTIALS_KEY, credentialsJson),
                JsonUtilities.createJsonPropertyToJson(PROFILE_KEY, profileTransportRepresentation())).asJson();
    }

    public String profileTransportRepresentation() throws JsonConversionException {
        return JsonUtilities.objectToJson(this);
    }

    /**
     * The users email address. Can be null
     * @return The users email address
     */
    @JsonIgnore
    public String getEmail() {
        return email;
    }

    /**
     * The users password. Can be null, and if the user has been logged in, it will be
     * @return The users password
     */
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    /**
     * Set the password value
     * @param password the new email value
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set the e-mail value
     * @param email the new email value
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public CMSessionToken getSessionToken() {
        if(sessionToken == null) {
            return CMSessionToken.FAILED;
        }
        return sessionToken;
    }

    @Override
    @JsonIgnore
    public String getObjectId() {
        return super.getObjectId();
    }

    /**
     * Check whether this user is logged in
     * @return true if the user is logged in successfully; false otherwise
     */
    @JsonIgnore
    public boolean isLoggedIn() {
        return sessionToken != null && sessionToken.isValid();
    }

    private boolean isCreated() {
        return getObjectId().equals(MISSING_OBJECT_ID) == false ||
                isLoggedIn();
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
            LoginResponse fakeResponse = createFakeLoginResponse();
            callback.onCompletion(fakeResponse);
            return;
        }
        CMWebService.getService().asyncLogin(this, setLoggedInUserCallback(callback));
    }

    public LoginResponse createFakeLoginResponse() {
        return new LoginResponse(getSessionToken().asJson());
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

    public void loadProfile() {
        loadProfile(Callback.DO_NOTHING);
    }

    public void loadProfile(final Callback callback) {
        if(isLoggedIn()) {
            loadAndMergeProfileUpdatesThenCallback(callback);
        } else {
            login(new LoginResponseCallback() {
                @Override
                public void onCompletion(LoginResponse response) {
                    loadAndMergeProfileUpdatesThenCallback(callback);
                }
            });
        }
    }

    private void loadAndMergeProfileUpdatesThenCallback(final Callback callback) {
        CMWebService.getService().getUserWebService(getSessionToken()).asyncLoadLoggedInUserProfile(new CMObjectResponseCallback() {
            @Override
            public void onCompletion(CMObjectResponse response) {
                try {
                    List<CMObject> loadedObjects = response.getObjects();
                    if(loadedObjects.size() == 1) {
                        CMObject thisUser = loadedObjects.get(0);
                        if(thisUser instanceof CMUser) { //this should always be true but nothin wrong with a little safety
                            mergeProfilesUpdates(((CMUser)thisUser).profileTransportRepresentation());
                        }
                    }
                }finally {
                    callback.onCompletion(response);
                }
            }
        });
    }

    private void mergeProfilesUpdates(String profileTransportRepresentation) {
        JsonUtilities.mergeJsonUpdates(this, profileTransportRepresentation);
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
     * See {@link #createUser(com.cloudmine.api.rest.callbacks.Callback)}
     */
    @Override
    public void save() throws CreationException, JsonConversionException {
        save(Callback.DO_NOTHING);
    }

    /**
     * If this has not been created, create the user. Otherwise, update the profile. If a user already exists on the
     * server, but it was not this instance that created it, and the user is not logged in, then this will attempt to
     * create the user and it will fail.<br>
     * In general, it is recommended that you either use {@link #saveProfile(com.cloudmine.api.rest.callbacks.Callback)}
     * or {@link #createUser(com.cloudmine.api.rest.callbacks.Callback)} instead of this method, so you can be explicit
     * about what you would like.
     */
    @Override
    public void save(Callback callback) throws CreationException, JsonConversionException {
        if(isCreated()) {
            saveProfile(callback);
        } else {
            createUser(callback);
        }
    }



    private void saveProfile(final Callback callback) {
        if(isLoggedIn()) {
            CMWebService.getService().getUserWebService(getSessionToken()).asyncInsertUserProfile(this, callback);
        } else {
            login(new LoginResponseCallback() {
                public void onCompletion(LoginResponse response) {
                    CMWebService.getService().getUserWebService(getSessionToken()).asyncInsertUserProfile(CMUser.this, callback);
                }
            });
        }
    }

    /**
     * See {@link #createUser(com.cloudmine.api.rest.callbacks.Callback)}
     */
    @Override
    public void saveWithUser(CMUser ignored) throws CreationException, JsonConversionException{
        saveWithUser(ignored, Callback.DO_NOTHING);
    }

    /**
     * See {@link #createUser(com.cloudmine.api.rest.callbacks.Callback)}
     */
    @Override
    public void saveWithUser(CMUser ignored, Callback callback) throws CreationException, JsonConversionException {
        save(callback);
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
        String userString = getEmail() + ":" + getPassword();
        return Base64Encoder.encode(userString);
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
                    clearPassword();
                    if(response.wasSuccess() &&
                            response.getSessionToken().isValid()) {
                        sessionToken = response.getSessionToken();
                        mergeProfilesUpdates(response.getProfileTransportRepresentation());
                    }
                }finally {
                    callback.onCompletion(response);
                }
            }
        };
    }

    private void clearPassword() {
        password = null;
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
        return getEmail() + ":" + getPassword();
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
