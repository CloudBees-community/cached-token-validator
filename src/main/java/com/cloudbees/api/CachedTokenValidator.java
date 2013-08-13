package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import java.util.concurrent.ExecutionException;

/**
 * Cached {@link TokenValidator}
 *
 * @author Kohsuke Kawaguchi
 */
class CachedTokenValidator extends TokenValidator {

    private final Cache<String,CachedToken> cache;

    /**
     * @param cb
     *      Partially configured cache.
     */
    CachedTokenValidator(CacheBuilder<Object,Object> cb, final TokenValidator base) {
        cache = cb.build(new CacheLoader<String,CachedToken>() {
            @Override
            public CachedToken load(String token) throws Exception {
                return new CachedToken(base.validateToken(token));
            }
        });
    }

    @Override
    public OauthToken validateToken(String token) throws OauthClientException {
        try {
            CachedToken cache = this.cache.get(token);
            OauthToken t = cache.get();
            if(t==null || t.isExpired()){
                return null;
            }
            return t;
        } catch (ExecutionException e) {
            // not unwrapping an exception to capture the call stack
            throw new OauthClientException(e);
        }
    }
}
