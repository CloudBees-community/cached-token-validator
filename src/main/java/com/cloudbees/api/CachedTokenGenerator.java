package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import com.cloudbees.api.oauth.TokenRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.*;

/**
 * Caching {@link TokenGenerator}.
 *
 * @author Kohsuke Kawaguchi
 */
class CachedTokenGenerator extends TokenGenerator {
    private final Cache<String,CachedToken> cache;

    /**
     * @param cb
     *      Partially configured cache.
     */
    CachedTokenGenerator(CacheBuilder<Object,Object> cb, final TokenGenerator base) {
        cache = cb.build(new CacheLoader<String,CachedToken>() {
            @Override
            public CachedToken load(String token) throws Exception {
                return new CachedToken(base.createToken(unpack(token)));
            }
        });
    }

    String pack(TokenRequest req) throws OauthClientException {
        try {
            return MAPPER.writeValueAsString(req);
        } catch (IOException e) {
            throw new OauthClientException("Failed to marshal TokenRequest",e);
        }
    }

    TokenRequest unpack(String packed) throws OauthClientException {
        try {
            return MAPPER.readValue(packed,TokenRequest.class);
        } catch (IOException e) {
            throw new OauthClientException("Failed to unmarshal TokenRequest",e);
        }
    }

    @Override
    public OauthToken createToken(TokenRequest tokenRequest) throws OauthClientException {
        try {
            String p = pack(tokenRequest);

            OauthToken t = getFromCache(p);
            if (t==null) {
                // definitely get a new value
                this.cache.invalidate(p);
                t = this.cache.get(p).get();
            }
            return t;
        } catch (ExecutionException e) {
            // not unwrapping an exception to capture the call stack
            throw new OauthClientException(e);
        }
    }

    /**
     * Gets the cached token valid in the cache, or if it's stale return null.
     */
    private OauthToken getFromCache(String p) throws ExecutionException {
        CachedToken cache = this.cache.get(p);

        if (cache.isHalfExpired())
            return null;

        OauthToken t = cache.get();
        if(t==null || t.isExpired()){
            return t;
        }
        return t;
    }

    /*package*/ static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibilityChecker(new Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.getDeserializationConfig().set(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

}
