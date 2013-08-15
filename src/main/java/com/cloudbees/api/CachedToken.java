package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthToken;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * Cached {@link OauthToken} with accurate expiration tracking.
 *
 * @author Kohsuke Kawaguchi
 */
final class CachedToken {
    /**
     * Null if the token was invalid to begin with.
     */
    private final @Nullable OauthToken token;
    private final long expiration;
    private final long halfExpiration;

    CachedToken(OauthToken token) {
        this.token = token;
        if (token!=null) {
            long now = System.currentTimeMillis();
            long e = TimeUnit.SECONDS.toMillis(token.getExpiresIn());
            expiration = now + e;
            halfExpiration = now + e /2;
        } else
            expiration = halfExpiration = -1;
    }

    /**
     * Returns true if half the life time of the token has elapsed
     * since the token was obtained. This really only makes sense
     * for caching token generation.
     */
    boolean isHalfExpired() {
        return halfExpiration < System.currentTimeMillis();
    }

    public @CheckForNull OauthToken get() {
        if (token==null)    return null;
        OauthToken t = token.clone();
        t.setExpiresIn(round(TimeUnit.MICROSECONDS.toSeconds(expiration - System.currentTimeMillis())));
        return t;
    }

    /**
     * Converts long to int by rounding values outside the range of int to the max/min values.
     */
    private int round(long l) {
        if (l>Integer.MAX_VALUE)    return Integer.MAX_VALUE;
        if (l<Integer.MIN_VALUE)    return Integer.MIN_VALUE;
        return (int)l;
    }
}
