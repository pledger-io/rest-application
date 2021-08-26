package com.jongsoft.finance.core;

/**
 * An Encoder makes it possible to encode values to a hash. This will ensure safety in the application.
 */
public interface Encoder {

    /**
     * Encrypt any random string value to a hash.
     *
     * @param value the value to hash
     * @return the hash value
     */
    String encrypt(String value);

    /**
     * Calculate if the provided hash and value match.
     *
     * @param encoded the hash value
     * @param value the raw value
     * @return true if they match
     */
    boolean matches(String encoded, String value);

}
