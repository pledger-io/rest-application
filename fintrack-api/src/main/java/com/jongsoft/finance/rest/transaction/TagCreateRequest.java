package com.jongsoft.finance.rest.transaction;

import io.micronaut.core.annotation.Introspected;

/**
 * The tag create request is used to add new tags to FinTrack
 */
@Introspected
public class TagCreateRequest {

    private String tag;

    private TagCreateRequest() {

    }

    public TagCreateRequest(String tag) {
        this.tag = tag;
    }

    /**
     * Get the actual tag name
     */
    public String getTag() {
        return tag;
    }

}
