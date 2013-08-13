package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClient;
import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import com.google.common.cache.CacheBuilder;

import javax.annotation.CheckForNull;

/**
 * Base interface for various token validators.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TokenValidator {
    /**
     * Validates token with the given scopes. Returns null if the given access token is invalid, otherwise OauthToken is returned.
     *
     * <p>
     * {@link BeesClient} must be constructed with OAuth client ID and client secret as the username and password.
     *
     * @param token non-null token
     * @param scopes array of scope that are expected to be granted for this token
     * @return null if the token is invalid such as expired or unknown to the CloudBees OAuth server or the expected
     * scopes are not found.
     */
    public final @CheckForNull OauthToken validateToken(String token, String... scopes) throws OauthClientException {
        OauthToken oauthToken = validateToken(token);
        if (oauthToken==null)   return null;

        if (oauthToken.validateScopes(scopes))
            return oauthToken;
        else
            return null;
    }

    /**
     * Obtains the details of the token and performs minimal validation (such as expiration.)
     * Returns null if the given access token is invalid, otherwise OauthToken is returned.
     *
     * <p>
     * {@link BeesClient} must be constructed with OAuth client ID and client secret as the username and password.
     *
     * @param token non-null token
     * @return null if the token is invalid such as expired or unknown to the CloudBees OAuth server.
     */
    public abstract @CheckForNull OauthToken validateToken(String token) throws OauthClientException;

    /**
     * Wraps this {@link TokenValidator} by adding caching.
     */
    public TokenValidator withCache(CacheBuilder<Object,Object>builder) {
        return new CachedTokenValidator(builder,this);
    }

    /**
     * Wraps this {@link TokenValidator} by adding some reasonable default caching behaviour.
     */
    public TokenValidator withCache() {
        return withCache(CacheBuilder.newBuilder()
                .maximumSize(65536));
    }

    /**
     * Wraps the {@link OauthClient} into {@link TokenValidator} without any decoration.
     *
     * The resulting validator does no caching.
     */
    public static TokenValidator from(final OauthClient client) {
        return new TokenValidator() {
            @Override
            public OauthToken validateToken(String token) throws OauthClientException {
                return client.validateToken(token);
            }
        };
    }

    public static TokenValidator from(BeesClient bees) {
        return from(bees.getOauthClient());
    }
}
