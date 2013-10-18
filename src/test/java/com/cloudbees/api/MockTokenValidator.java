package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import org.codehaus.jackson.annotate.JsonProperty;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link TokenValidator} for testing purpose.
 *
 * This implementation is useful for unit testing your code that uses {@link TokenValidator}
 * so that you can run offline without talking to the actual grand central.
 *
 * This implementation expects the access token in the format
 * "key=value,key=value,key=value,..." where each key is the JSON property name
 * of the {@link OauthToken}, such as "account" and "access_token".
 *
 * If you pass in the token that starts with "invalid", the token validator pretends that
 * the token have failed validation.
 *
 * @author Kohsuke Kawaguchi
 */
public class MockTokenValidator extends TokenValidator {
    @Override
    public OauthToken validateToken(String token) throws OauthClientException {

        // cue to return null?
        if (token == null || token.startsWith("invalid"))
            return null;

        OauthToken oa = new OauthToken();
        for (String t : token.split(",")) {
            String[] lr = t.split("=");
            if (lr.length!=2)
                throw new IllegalArgumentException("Malformed mock token: "+t);

            Field f = FIELDS.get(lr[0]);
            if (f==null)
                throw new IllegalArgumentException("Invalid property: "+lr[0]+" expecting one of "+FIELDS.keySet());

            try {
                f.set(oa,lr[1]);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }

        return oa;
    }

    private static final Map<String,Field> FIELDS = new HashMap<String, Field>();

    static {
        for (Field f : OauthToken.class.getDeclaredFields()) {
            f.setAccessible(true);
            JsonProperty p = f.getAnnotation(JsonProperty.class);
            String name = p!=null ? p.value() : f.getName();
            FIELDS.put(name,f);
        }

    }
}
