package org.mockito.configuration;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * This class is used to configure Mockito's default behavior.
 * It implements the IMockitoConfiguration interface.
 */
@SuppressWarnings("unused")
public class MockitoConfiguration implements IMockitoConfiguration {

    /**
     * This method is used to provide a default answer for all methods of a mock that are not stubbed.
     * It overrides the getDefaultAnswer method from the IMockitoConfiguration interface.
     * If the return type of the method is assignable from Optional, it returns Control.Option().
     * Otherwise, it uses the super class's answer method to return default values.
     *
     * @return An Answer<Object> that provides the default answer for unstubbed methods.
     */
    @Override
    public Answer<Object> getDefaultAnswer() {
        return new ReturnsEmptyValues() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                if (Optional.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
                    return Control.Option();
                }
                if (Sequence.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
                    return Collections.List();
                }
                if (ResultPage.class.isAssignableFrom(invocation.getMethod().getReturnType())) {
                    return ResultPage.empty();
                }
                return super.answer(invocation);
            }

        };
    }

    /**
     * This method is used to determine whether Mockito should clean the stack trace.
     * It overrides the cleansStackTrace method from the IMockitoConfiguration interface.
     *
     * @return A boolean value indicating whether Mockito should clean the stack trace. It always returns false.
     */
    @Override
    public boolean cleansStackTrace() {
        return false;
    }

    /**
     * This method is used to determine whether Mockito should enable the class cache.
     * It overrides the enableClassCache method from the IMockitoConfiguration interface.
     *
     * @return A boolean value indicating whether Mockito should enable the class cache. It always returns false.
     */
    @Override
    public boolean enableClassCache() {
        return false;
    }
}
