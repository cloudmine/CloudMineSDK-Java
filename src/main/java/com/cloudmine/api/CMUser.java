package com.cloudmine.api;

import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.NotLoggedInException;
import com.cloudmine.api.rest.CMSocial;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.UserCMWebService;
import com.cloudmine.api.rest.callbacks.*;
import com.cloudmine.api.rest.options.CMRequestOptions;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.CMResponse;
import com.cloudmine.api.rest.response.CreationResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A CMUser consists of an email and a password. When logged in, objects can be specified to be saved
 * at the {@link ObjectLevel.USER}, in which case they must be loaded and saved using the {@link CMSessionToken}
 * obtained by logging in as their associated CMUser. CMUser objects should be instantiated through the static {@link #CMUser(String, String)}
 * function, as platform specific implementations may be necessary.<BR>
 * If you extend CMUser (to allow for profile information), you must provide a no args constructor.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMUser extends CMObject {
    private static final Logger LOG = LoggerFactory.getLogger(CMUser.class);

    public static final String MISSING_VALUE = "unset";
    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";
    public static final String CREDENTIALS_KEY = "credentials";
    public static final String PROFILE_KEY = "profile";
    public static final String CLASS_NAME = "CMUser";

    /**
     * Search the user profiles for the given string. For more information on the format, see <a href="https://cloudmine.me/docs/object-storage#object_search">the CloudMine documentation on search</a> <br>
     * For example, to search for all users with the field age, where age is > 30, the searchString=[age>30]
     * @param searchString what to search for
     * @param callback will be called after load. Expects a {@link CMObjectResponse}. It is recommended that {@link CMObjectResponseCallback} is used here
     */
    public static void searchUserProfiles(String searchString, Callback<CMObjectResponse> callback) {
        searchUserProfiles(searchString, CMRequestOptions.NONE, callback);
    }
    /**
     * Search the user profiles for the given string. For more information on the format, see <a href="https://cloudmine.me/docs/object-storage#object_search">the CloudMine documentation on search</a> <br>
     * For example, to search for all users with the field age, where age is > 30, the searchString=[age>30]
     * @param searchString what to search for
     * @param callback will be called after load. Expects a {@link CMObjectResponse}. It is recommended that {@link CMObjectResponseCallback} is used here
     */
    public static void searchUserProfiles(String searchString, CMRequestOptions options, Callback<CMObjectResponse> callback) {
        CMWebService.getService().asyncSearchUserProfiles(searchString, options, callback);
    }

    /**
     * Load all the user profiles for this application. User profiles include the user id and any profile information,
     * but not the user's e-mail address (unless e-mail address is an additional field added to profile).
     * @param callback A callback that expects a {@link CMObjectResponse}. It is recommended that a {@link CMObjectResponseCallback} is used here
     */
    public static void loadAllUserProfiles(Callback<CMObjectResponse> callback) {
        CMWebService.getService().asyncLoadAllUserProfiles(callback);
    }

    /**
     * Get the profile for the user given
     * @param callback A callback that expects a {@link CMObjectResponse}. It is recommended that a {@link CMObjectResponseCallback} is used here
     */
    public static void loadLoggedInUserProfile(final CMUser user, final Callback<CMObjectResponse> callback) throws CreationException{
        if(user.isLoggedIn()) {
            CMWebService.getService().getUserWebService(user.getSessionToken()).asyncLoadLoggedInUserProfile(callback);
        } else {
            user.login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
                @Override
                public void onCompletion(LoginResponse response) {
                    CMWebService.getService().getUserWebService(response.getSessionToken()).asyncLoadLoggedInUserProfile(callback);
                }
            });
        }

    }


    private String email;
    private String password;
    private CMSessionToken sessionToken;
    private Set<CMSocial.Service> authenticatedServices = EnumSet.noneOf(CMSocial.Service.class);

    protected CMUser() {
        this(MISSING_VALUE, MISSING_VALUE);
    }
    /**
     * Instantiate a new CMUser instance with the given email and password
     * @param email email of the user
     * @param password password for the user
     */
    public CMUser(String email, String password) {
        super(false);
        this.email = email;
        this.password = password;
    }

    /**
     * Instantiate a new CMUser instance with the given email. Password will be blank and should be set before logging in
     * @param email email of the user
     */
    public CMUser(String email) {
        this(email, "");
    }

    public String transportableRepresentation() throws ConversionException {
        String credentialsJson = JsonUtilities.jsonCollection(
                                        JsonUtilities.createJsonProperty(EMAIL_KEY, getEmail()),
                                        JsonUtilities.createJsonProperty(PASSWORD_KEY, getPassword())).transportableRepresentation();
        return JsonUtilities.jsonCollection(
                JsonUtilities.createJsonPropertyToJson(CREDENTIALS_KEY, credentialsJson),
                JsonUtilities.createJsonPropertyToJson(PROFILE_KEY, profileTransportRepresentation())).transportableRepresentation();
    }

    public String profileTransportRepresentation() throws ConversionException {
        return JsonUtilities.objectToJson(this);
    }

    @Override
    public String getClassName() {
        if(getClass() == CMUser.class) //this way if someone extends this, it will not say this is a CMUser, but whatever their subclass is
            return CLASS_NAME;
        return super.getClassName();
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

    public void setSessionToken(CMSessionToken token) {
        this.sessionToken = token;
    }

    @JsonProperty("__services__")
    public void setAuthenticatedServices(Set<CMSocial.Service> services) {
        authenticatedServices = services;
    }

    @JsonProperty("__services__")
    public Set<CMSocial.Service> getAuthenticatedServices() {
        return new HashSet<CMSocial.Service>(authenticatedServices);
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

    @Override
    @JsonProperty(JsonUtilities.OBJECT_ID_KEY)
    public void setObjectId(String objectId) {
        super.setObjectId(objectId);
    }

    /**
     * Check whether this user is logged in
     * @return true if the user is logged in successfully; false otherwise
     * @deprecated relies on {@link com.cloudmine.api.CMSessionToken#isValid()} and is deprecated for the same reason
     */
    @JsonIgnore
    @Deprecated
    public boolean isLoggedIn() {
        return sessionToken != null && sessionToken.isValid();
    }

    /**
     * Whether there is enough information set on this user to attempt a login. Required information is a password and
     * either a username or e-mail
     * @return true if a log in attempt could be made
     */
    public boolean isLoginAttemptPossible() {
        return Strings.isNotEmpty(password) &&
                (Strings.isNotEmpty(email));
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
        login(CMCallback.<LoginResponse>doNothing());
    }

    /**
     * Asynchronously log in this user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link LoginResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.LoginResponseCallback} is passed in
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void login(Callback<LoginResponse> callback) throws CreationException {
        if(isLoggedIn()) {
            LoginResponse fakeResponse = createFakeLoginResponse();
            callback.onCompletion(fakeResponse);
            return;
        }
        CMWebService.getService().asyncLogin(this, setLoggedInUserCallback(callback));
    }

    public LoginResponse createFakeLoginResponse() {
        return new LoginResponse(getSessionToken().transportableRepresentation());
    }

    /**
     * Asynchronously log out this user
     */
    public void logout() {
        logout(CMCallback.<CMResponse>doNothing());
    }

    /**
     * Asynchronously log out this user
     * @param callback a {@link Callback} that expects a {@link CMResponse}. It is recommended that a {@link CMResponseCallback} is used here
     */
    public void logout(Callback<CMResponse> callback) {
        CMWebService.getService().asyncLogout(getSessionToken(), setLoggedOutUserCallback(callback));
    }

    private void loadProfile() {
        loadProfile(CMCallback.<CMObjectResponse>doNothing());
    }

    private void loadProfile(final Callback<CMObjectResponse> callback) {
        if(isLoggedIn()) {
            loadAndMergeProfileUpdatesThenCallback(callback);
        } else {
            login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
                @Override
                public void onCompletion(LoginResponse response) {
                    loadAndMergeProfileUpdatesThenCallback(callback);
                }
            });
        }
    }

    public void loadAccessLists() {
        loadAccessLists(CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Change this user's email address. Note that the password must be set; if the user has been logged in, the password
     * has been cleared and must be reset. The user's old session tokens will be invalid upon completion of this operation
     * @param newEmail The new e-mail address
     */
    public void changeEmailAddress(String newEmail) {
        changeEmailAddress(newEmail, CMCallback.<CMResponse>doNothing());
    }

    /**
     * Change this user's email address. Note that the password must be set; if the user has been logged in, the password
     * has been cleared and must be reset. The user's old session tokens will be invalid upon completion of this operation
     * @param newEmail The new e-mail address
     * @param callback
     */
    public void changeEmailAddress(String newEmail, Callback<CMResponse> callback) {
        CMWebService.getService().asyncChangeEmail(this, newEmail, callback);
    }

    /**
     * Change this user's email address. The user's old session tokens will be invalid upon completion of this operation
     * @param newEmail the new e-mail address
     * @param currentPassword
     * @param callback
     */
    public void changeEmailAddress(String newEmail, String currentPassword, Callback<CMResponse> callback) {
        setPassword(currentPassword);
        changeEmailAddress(newEmail, callback);
    }

    /**
     * Change this user's email address. The user's old session tokens will be invalid upon completion of this operation
     * @param newEmail the new e-mail address
     * @param currentPassword
     */
    public void changeEmailAddress(String newEmail, String currentPassword) {
        setPassword(currentPassword);
        changeEmailAddress(newEmail);
    }

    /**
     * Load the {@link CMAccessList} that  belong to this user. If this user is not logged in, they will be.
     * @param callback expects a {@link CMObjectResponse}. It is recommended that a {@link CMObjectResponseCallback} is passed in here
     */
    public void loadAccessLists(final Callback<CMObjectResponse> callback) {
        if(isLoggedIn()) {
            getUserService().asyncLoadAccessLists(callback);
        } else {
            login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
                public void onCompletion(LoginResponse response) {
                    if(isLoggedIn()) {
                        getUserService().asyncLoadAccessLists(callback);
                    } else {
                        callback.onFailure(new NotLoggedInException("Was unable to log in user"), "Wasn't logged in");
                    }
                }
            });
        }
    }

    private UserCMWebService getUserService() {
        return CMWebService.getService().getUserWebService(getSessionToken());
    }

    private void loadAndMergeProfileUpdatesThenCallback(final Callback callback) {
        if(isLoggedIn()) {
            getUserService().asyncLoadLoggedInUserProfile(new ExceptionPassthroughCallback<CMObjectResponse>(callback) {
                @Override
                public void onCompletion(CMObjectResponse response) {
                    try {
                        List<CMObject> loadedObjects = response.getObjects();
                        if (loadedObjects.size() == 1) {
                            CMObject thisUser = loadedObjects.get(0);
                            if (thisUser instanceof CMUser) { //this should always be true but nothin wrong with a little safety
                                mergeProfilesUpdates(((CMUser) thisUser).profileTransportRepresentation());
                            } else {
                                LOG.error("Loaded user profile that isn't a CMUser");
                            }
                        } else {
                            LOG.error("Loaded multiple user profiles for a single user");
                        }
                    } finally {
                        callback.onCompletion(response);
                    }
                }
            });
        }else {
            callback.onFailure(new NotLoggedInException("Was unable to log in"), "Unable to log in");
        }
    }

    private void mergeProfilesUpdates(String profileTransportRepresentation) {
        JsonUtilities.mergeJsonUpdates(this, profileTransportRepresentation);
    }

    /**
     * Asynchronously create this user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link com.cloudmine.api.rest.response.CreationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CreationResponseCallback} is passed in
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing this and doing something you shouldn't be with overriding transportableRepresentation
     */
    public void createUser(Callback<CreationResponse> callback) throws CreationException, ConversionException {
        CMWebService.getService().asyncCreateUser(this, callback);
    }

    /**
     * Equivalent to {@link #createUser(com.cloudmine.api.rest.callbacks.Callback)} with no callback
     */
    public void createUser() throws CreationException, ConversionException {
        createUser(CMCallback.<CreationResponse>doNothing());
    }

    /**
     * See {@link #createUser(com.cloudmine.api.rest.callbacks.Callback)}
     */
    @Override
    @Deprecated
    public void save() throws CreationException, ConversionException {
        save(CMCallback.doNothing());
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
    @Deprecated
    public void save(Callback callback) throws CreationException, ConversionException {
        if(isCreated()) {
            saveProfile(callback);
        } else {
            createUser(callback);
        }
    }


    /**
     * Save the profile of this user; this should be used instead of {@link #save()} if you know the user has already
     * been created. Will log the user in if the user is not already logged in
     * @param callback expects a {@link CreationResponse}. It is recommended a {@link CreationResponseCallback} is used here
     */
    public void saveProfile(final Callback<CreationResponse> callback) {
        if(isLoggedIn()) {
            getUserService().asyncInsertUserProfile(this, callback);
        } else {
            login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
                public void onCompletion(LoginResponse response) {
                    if(isLoggedIn()) {
                        getUserService().asyncInsertUserProfile(CMUser.this, callback);
                    } else {
                        callback.onFailure(new NotLoggedInException("Unable to log in"), "Unable to log in");
                    }
                }
            });
        }
    }

    /**
     * See {@link #createUser(com.cloudmine.api.rest.callbacks.Callback)}
     */
    @Override
    @Deprecated
    public void saveWithUser(CMUser ignored) throws CreationException, ConversionException{
        saveWithUser(ignored, CMCallback.doNothing());
    }

    /**
     * Use either {@link #createUser(com.cloudmine.api.rest.callbacks.Callback)} or {@link #saveProfile(com.cloudmine.api.rest.callbacks.Callback)}
     */
    @Override
    @Deprecated
    public void saveWithUser(CMUser ignored, Callback callback) throws CreationException, ConversionException {
        save(callback);
    }

    /**
     * Asynchronously change this users password
     * @param newPassword the new password
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     * @deprecated in favor of {@link #changePassword(String, String, com.cloudmine.api.rest.callbacks.Callback)}
     */
    @Deprecated
    public void changePassword(String newPassword) throws CreationException {
        changePassword(newPassword, CMCallback.<CMResponse>doNothing());
    }

    /**
     * Asynchronously change this users password. This only works if the user has not been logged in, and
     * should be avoided in favor of {@link #changePassword(String, String, com.cloudmine.api.rest.callbacks.Callback)}
     * @param newPassword the new password
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     * @deprecated in favor of {@link #changePassword(String, String, com.cloudmine.api.rest.callbacks.Callback)}
     */
    @Deprecated
    public void changePassword(String newPassword, Callback<CMResponse> callback) throws CreationException {
        changePassword(getPassword(), newPassword, callback);
    }

    /**
     * Change this user's password from the oldPassword to the newPassword.
     * @param oldPassword the old (current) password
     * @param newPassword the new password
     * @param options any options to use for this call, IE a CMServerFunction to run after the password has been updated
     * @param callback callback to run once the password has been changed. Use a {@link CMResponseCallback} here
     */
    public void changePassword(String oldPassword, String newPassword, CMRequestOptions options, Callback<CMResponse> callback) {
        CMWebService.getService().asyncChangePassword(this.getEmail(), oldPassword, newPassword, options, callback);
        setPassword(newPassword);
    }

    /**
     * See {@link #changePassword(String, String, com.cloudmine.api.rest.options.CMRequestOptions, com.cloudmine.api.rest.callbacks.Callback)}
     * @param oldPassword
     * @param newPassword
     * @param callback
     * @throws CreationException
     */
    public void changePassword(String oldPassword, String newPassword, Callback<CMResponse> callback) throws CreationException {
        changePassword(oldPassword, newPassword, CMRequestOptions.NONE, callback);
    }

    /**
     * See {@link #changePassword(String, String, com.cloudmine.api.rest.options.CMRequestOptions, com.cloudmine.api.rest.callbacks.Callback)}
     * @param oldPassword
     * @param newPassword
     * @throws CreationException
     */
    public void changePassword(String oldPassword, String newPassword) throws CreationException {
        changePassword(oldPassword, newPassword, CMRequestOptions.NONE, CMCallback.<CMResponse>doNothing());
    }

    /**
     * Asynchronously Request that this user's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void resetPasswordRequest() throws CreationException {
        resetPasswordRequest(CMCallback.<CMResponse>doNothing());
    }

    /**
     * Asynchronously Request that this user's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void resetPasswordRequest(Callback<CMResponse> callback) throws CreationException {
        CMWebService.getService().asyncResetPasswordRequest(getEmail(), callback);
    }

    /**
     * Asynchronously confirm that the users password should be reset. Requires the token sent to the user's email address
     * @param emailToken from the e-mail sent to the user
     * @param newPassword the new password
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void resetPasswordConfirmation(String emailToken, String newPassword) throws CreationException {
        resetPasswordConfirmation(emailToken, newPassword, CMCallback.<CMResponse>doNothing());
    }

    /**
     * Asynchronously confirm that the users password should be reset. Requires the token sent to the user's email address
     * @param emailToken from the e-mail sent to the user
     * @param newPassword the new password
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @throws CreationException if called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public void resetPasswordConfirmation(String emailToken, String newPassword, Callback<CMResponse> callback) throws CreationException {
        CMWebService.getService().asyncResetPasswordConfirmation(emailToken, newPassword, callback);
    }

    public static String encode(String email, String password) {
        return LibrarySpecificClassCreator.getCreator().getEncoder().encode(email + ":" + password);
    }

    /**
     * Encode this CMUser's email and password as a Base64 string. The format is email:password
     * @return a Base64 representation of this user
     */
    public String encode() {
        return encode(getEmail(), getPassword());
    }

    private final Callback<LoginResponse> setLoggedInUserCallback(final Callback<LoginResponse> callback) {
        return new ExceptionPassthroughCallback<LoginResponse>(callback) {
            @Override
            public void onCompletion(LoginResponse response) {
                try {
                    clearPassword();
                    if(response.wasSuccess() &&
                            response.getSessionToken() != null &&
                            response.getSessionToken().isValid()) { //this call is still valid since non of the reasons for deprecating this method can apply at this point
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

    private final CMResponseCallback setLoggedOutUserCallback(final Callback<CMResponse> callback) {
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
        return getEmail() + ":" + getPassword() + ":" + getAuthenticatedServices();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMUser cmUser = (CMUser) o;

        if (authenticatedServices != null ? !authenticatedServices.equals(cmUser.authenticatedServices) : cmUser.authenticatedServices != null)
            return false;
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
        result = 31 * result + (authenticatedServices != null ? authenticatedServices.hashCode() : 0);
        return result;
    }
}
