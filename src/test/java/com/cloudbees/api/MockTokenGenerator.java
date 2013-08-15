package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import com.cloudbees.api.oauth.TokenRequest;

import java.util.List;

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
    @Override
    public OauthToken createToken(TokenRequest r) throws OauthClientException {
        OauthToken t = new OauthToken();
        t.accessToken = "account="+r.getAccountName()+",scope="+ join(r.getScopes()," ");
        return t;
    }

    private String join(List<String> values, String delim) {
        StringBuilder buf = new StringBuilder();
        for (String value : values) {
            if (buf.length()>0)
                buf.append(delim);
            buf.append(value);
        }
        return buf.toString();
    }
}
