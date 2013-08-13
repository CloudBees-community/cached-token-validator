package com.cloudbees.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class CacheRetryTest {
    /**
     * If the cache computation fails, the next thread should retry.
     */
    @Test
    public void checkRetryBehavior() throws Exception {
        Cache<Object,Object> b = CacheBuilder.newBuilder().build(new CacheLoader<Object, Object>() {
            int count = 0;

            @Override
            public Object load(Object key) throws Exception {
                if ((count++) % 2 == 0)
                    throw new Exception();
                return "foo";
            }
        });

        try {
            b.get(0);
            fail();
        } catch (ExecutionException e) {
            assertEquals(Exception.class,e.getCause().getClass());
        }

        assertEquals("foo",b.get(0));
    }
}
