package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClient;
import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import com.cloudbees.api.oauth.TokenRequest;
import com.google.common.cache.CacheBuilder;

import java.util.Arrays;
import java.util.Collection;

/**
 * Base interface for various token generators.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TokenGenerator {
    /**
     * Creates a OAuth token for the current user (used to create {@link BeesClient})
     *
     * @return OauthToken. always non-null if there was error such as invalid credentials
     * @throws OauthClientException if there is any error during token validation
     */
    public abstract OauthToken createToken(TokenRequest tokenRequest) throws OauthClientException;

    /**
     * OAuth client application can use this method to create an OAuth token with arbitrary scopes
     * that belongs to the user who registered the application.
     *
     * The created token will be tied only to the account that the OAuth client application is registered with,
     * even if the user who registered it may have access to other accounts.
     *
     * <p>
     * For this method to work, {@link BeesClient} should be called with OAuth client ID and secret.
     *
     * @see <a href="http://wiki.cloudbees.com/bin/view/RUN/OAuth#HServerApplication">Wiki</a>
     *
     * @return never null. In case of a problem, an exception will be thrown.
     */
    public abstract OauthToken createOAuthClientToken(Collection<String> scopes) throws OauthClientException;

    /**
     * Overloaded version of {@link #createOAuthClientToken(Collection)}
     */
    public final OauthToken createOAuthClientToken(String... scopes) throws OauthClientException {
        return createOAuthClientToken(Arrays.asList(scopes));
    }

    /**
     * Wraps this {@link CachedTokenGenerator} by adding caching.
     */
    public TokenGenerator withCache(CacheBuilder<Object,Object> builder) {
        return new CachedTokenGenerator(builder,this);
    }

    /**
     * Wraps this {@link CachedTokenGenerator} by adding some reasonable default caching behaviour.
     */
    public TokenGenerator withCache() {
        return withCache(CacheBuilder.newBuilder().maximumSize(65536));
    }

    /**
     * Wraps the {@link OauthClient} into {@link TokenValidator} without any decoration.
     *
     * The resulting validator does no caching.
     */
    public static TokenGenerator from(final OauthClient client) {
        return new TokenGenerator() {
            @Override
            public OauthToken createToken(TokenRequest tokenRequest) throws OauthClientException {
                return client.createToken(tokenRequest);
            }

            @Override
            public OauthToken createOAuthClientToken(Collection<String> scopes) throws OauthClientException {
                return client.createOAuthClientToken(scopes);
            }
        };
    }

    public static TokenGenerator from(BeesClient bees) {
        return from(bees.getOauthClient());
    }
}
