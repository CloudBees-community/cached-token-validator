package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
class CascadingTokenValidator extends TokenValidator {
    private final TokenValidator lhs,rhs;

    CascadingTokenValidator(TokenValidator lhs, TokenValidator rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public OauthToken validateToken(String token) throws OauthClientException {
        try {
            OauthToken t = lhs.validateToken(token);
            if (t!=null)
                return t;
        } catch (OauthClientException e) {
            LOGGER.log(Level.WARNING, "Failed to validate token with "+lhs,e);
        }

        return rhs.validateToken(token);
    }

    private static final Logger LOGGER = Logger.getLogger(CascadingTokenValidator.class.getName());
}
