package com.jongsoft.finance.spending.domain.service;

import static org.mockito.Mockito.*;

import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.finance.spending.domain.model.AnalyzeJob;
import com.jongsoft.lang.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.YearMonth;

@DisplayName("Unit - Spending Analysis Scheduler")
class SpendingAnalysisSchedulerTest {

    @Test
    @DisplayName("Should schedule analysis for the previous completed month")
    void shouldScheduleAnalysisForPreviousMonth() {
        UserProvider userProvider = mock(UserProvider.class);
        UserAccount user = mock(UserAccount.class);
        UserIdentifier userIdentifier = new UserIdentifier("user@example.com");

        when(user.getUsername()).thenReturn(userIdentifier);
        when(userProvider.lookup()).thenReturn(Collections.List(user));

        SpendingAnalysisScheduler scheduler = new SpendingAnalysisScheduler(userProvider);
        YearMonth expectedMonth = YearMonth.now().minusMonths(1);

        try (MockedStatic<InternalAuthenticationEvent> auth =
                        mockStatic(InternalAuthenticationEvent.class);
                MockedStatic<AnalyzeJob> jobs = mockStatic(AnalyzeJob.class)) {
            scheduler.analyzeMonthlySpendingPatterns();

            auth.verify(() -> InternalAuthenticationEvent.authenticate("user@example.com"));
            jobs.verify(() -> AnalyzeJob.create(userIdentifier, expectedMonth));
        }
    }
}
