package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import com.cloudbees.api.oauth.TokenRequest;

import java.util.Collection;

import static com.cloudbees.api.StringUtils.*;

/**
 * {@link TokenGenerator} for testing purpose.
 *
 * This implementation is useful for unit testing your code that uses {@link TokenGenerator}
 * so that you can run offline without talking to the actual grand central.
 *
 * <p>
 * It generates the token in the format {@link MockTokenValidator} understands.
 *
 * @author Kohsuke Kawaguchi
 */
public class MockTokenGenerator extends TokenGenerator {
    private final String account;

    /**
     * @param account
     *      {@link #createOAuthClientToken(Collection)} will generate tokens tied to this account.
     *      In real Grand Central, this corresponds to the account under which the application is registered.
     */
    public MockTokenGenerator(String account) {
        this.account = account;
    }

    @Override
    public OauthToken createToken(TokenRequest r) throws OauthClientException {
        OauthToken t = new OauthToken();
        t.accessToken = "account="+r.getAccountName()+",scope="+ join(r.getScopes(), " ");
        return t;
    }

    @Override
    public OauthToken createOAuthClientToken(Collection<String> scopes) throws OauthClientException {
        OauthToken t = new OauthToken();
        t.accessToken = "account="+account+",scope="+ join(scopes, " ");
        return t;
    }
}
