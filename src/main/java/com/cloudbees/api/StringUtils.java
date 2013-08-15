package com.cloudbees.api;

import java.util.Collection;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
/*package*/ class StringUtils {
    static  String join(Collection<String> values, String delim) {
        StringBuilder buf = new StringBuilder();
        for (String value : values) {
            if (buf.length()>0)
                buf.append(delim);
            buf.append(value);
        }
        return buf.toString();
    }
}
