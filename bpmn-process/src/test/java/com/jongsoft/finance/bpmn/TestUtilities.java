package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.ProcessMapper;
import io.micronaut.serde.ObjectMapper;

public class TestUtilities {

    /**
     * Get a {@link ProcessMapper} that uses the default {@link ObjectMapper}.
     *
     * @return a {@link ProcessMapper} that uses the default {@link ObjectMapper}
     */
    public static ProcessMapper getProcessMapper() {
        return new ProcessMapper(ObjectMapper.getDefault());
    }

}
