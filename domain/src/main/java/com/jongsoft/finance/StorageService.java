package com.jongsoft.finance;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.lang.control.Optional;

public interface StorageService extends JavaBean {

    /**
     * Creates a file on disk containing the given content.
     *
     * @param content
     * @return the token that can be used to retrieve the file
     */
    String store(byte[] content);

    Optional<byte[]> read(String token);

    /**
     * Remove a file from storage that is no longer needed.
     *
     * @param token the token of the file
     */
    void remove(String token);
}
