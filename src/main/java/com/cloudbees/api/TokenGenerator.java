package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClient;
import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import com.cloudbees.api.oauth.TokenRequest;
import com.google.common.cache.CacheBuilder;

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
        };
    }

    public static TokenGenerator from(BeesClient bees) {
        return from(bees.getOauthClient());
    }
}
