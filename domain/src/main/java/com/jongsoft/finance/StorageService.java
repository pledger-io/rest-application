package com.jongsoft.finance;

import io.reactivex.Single;

public interface StorageService {

    /**
     * Creates a file on disk containing the given content.
     *
     * @param content
     * @return  the token that can be used to retrieve the file
     */
    String store(byte[] content);

    Single<byte[]> read(String token);

    /**
     * Remove a file from storage that is no longer needed.
     *
     * @param token the token of the file
     */
    void remove(String token);

}
