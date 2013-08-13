OAuth Token Validation Caching
==============================

This little addition on top of [cloudbees-api-client](https://github.com/cloudbees/cloudbees-api-client) provides
an OAuth token validator with the decoration pattern of adding in-memory client-side caching.

This reduces the number of calls to the OAuth server (aka Grand Central), and allows the client to continue
service even in the case of the OAuth server outage.


Usage
-----
    // if you are validating tokens, you are most likely OAuth client yourself.
    BeesClient bees = new BeesClient(clientId,clientSecret);

    // this creates non-cached plain-vanilla validator
    TokenValidator v = TokenValidator.from(bees);

    // decorates v with caching
    v = v.withCache();

    ...

    OauthToken t = v.validateToken(tokenThatCameFromHttpRequest)
    if (t==null)    throw new SecurityException("invalid token: "+tokenThatCameFromHttpRequest);
