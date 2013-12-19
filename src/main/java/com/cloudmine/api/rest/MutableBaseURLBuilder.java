package com.cloudmine.api.rest;

/**
 * Created with IntelliJ IDEA.
 * User: johnmccarthy
 * Date: 5/6/13
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MutableBaseURLBuilder<T> extends BaseURLBuilder<T> {

    final StringBuilder baseUrl;
    final StringBuilder actions;
    final StringBuilder queryParams;

    public MutableBaseURLBuilder(String baseUrl) {
        this(baseUrl, "", "");
    }

    protected MutableBaseURLBuilder(String baseUrl, String actions, String queryParams) {
        this.baseUrl = new StringBuilder(baseUrl);
        this.actions = new StringBuilder(actions);
        this.queryParams = new StringBuilder(queryParams);
    }

    protected String querySeparator() {
        return queryParams.length() == 0 ?
                FIRST_QUERY_SEPARATOR :
                SUBSEQUENT_QUERY_SEPARATOR;
    }

    public T addAction(String action) {
        actions.append(formatUrlPart(action));
        return (T)this;
    }

    public T removeAction(String action) {

        int startPosition = actions.indexOf(action);
        int endPosition = action.length() + startPosition;
        if(startPosition >= 0 &&
                startPosition < endPosition && endPosition <= actions.length()) {
            actions.delete(startPosition, endPosition);
            int doubleSlashPosition = actions.indexOf("//");
            if(doubleSlashPosition > -1) {
                actions.deleteCharAt(doubleSlashPosition);
            } else {
                int lastCharPosition = actions.length() - 1;
                if(lastCharPosition > -1 && actions.charAt(lastCharPosition) == '/') {
                    actions.deleteCharAt(lastCharPosition);
                }
            }
        }
        return (T)this;
    }


    public T addQuery(String key, int value) {
        addQuery(key, String.valueOf(value));
        return (T) this;
    }

    public T addQuery(String key, String value) {
        queryParams.append(querySeparator()).append(key).append(QUERY_CONNECTOR).append(value);
        return (T) this;
    }

    public T addQuery(String query) {
        queryParams.append(querySeparator()).append(query);
        return (T)this;
    }

    public String getBaseUrl() {
        return baseUrl.toString();
    }

    public String asUrlString() {
        return baseUrl.toString() + actions.toString() + queryParams.toString();
    }
}
