package com.jongsoft.finance.spending.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.adapter.mail.MailDaemon;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.finance.spending.adapter.api.SpendingInsightProvider;
import com.jongsoft.finance.spending.adapter.api.SpendingPatternProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Tag("unit")
@DisplayName("Unit - Spending Report Scheduler")
class SpendingReportSchedulerTest {

    private SpendingInsightProvider spendingInsightProvider;
    private SpendingPatternProvider spendingPatternProvider;
    private CurrentUserProvider currentUserProvider;
    private MailDaemon mailDaemon;
    private SpendingReportScheduler scheduler;
    private UserAccount mockUser;
    private UserIdentifier mockUserIdentifier;

    @BeforeEach
    void setUp() {
        // Create mocks
        spendingInsightProvider = mock(SpendingInsightProvider.class);
        spendingPatternProvider = mock(SpendingPatternProvider.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        mailDaemon = mock(MailDaemon.class);
        mockUser = mock(UserAccount.class);
        mockUserIdentifier = mock(UserIdentifier.class);

        // Set up user mock
        when(mockUser.getUsername()).thenReturn(mockUserIdentifier);
        when(mockUserIdentifier.email()).thenReturn("test@example.com");
        when(currentUserProvider.currentUser()).thenReturn(mockUser);

        // Create the scheduler
        scheduler = new SpendingReportScheduler(
                spendingInsightProvider, spendingPatternProvider, currentUserProvider, mailDaemon);
    }

    @Test
    @DisplayName("Should send email with correct data")
    void shouldSendEmailWithCorrectData() {
        // Arrange
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        String formattedMonth = previousMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        // Mock the providers to return non-null values
        // We don't need to mock the actual Sequence or its contents for this test
        when(spendingInsightProvider.lookup(previousMonth))
                .thenReturn(mock(com.jongsoft.lang.collection.Sequence.class));
        when(spendingPatternProvider.lookup(previousMonth))
                .thenReturn(mock(com.jongsoft.lang.collection.Sequence.class));

        // Act
        scheduler.analyzeMonthlySpendingPatterns();

        // Assert
        // Capture the properties passed to the mail daemon
        ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
        verify(mailDaemon)
                .send(eq("test@example.com"), eq("spending-report"), propertiesCaptor.capture());

        Properties capturedProperties = propertiesCaptor.getValue();
        assertEquals(mockUser, capturedProperties.get("user"));
        assertEquals(formattedMonth, capturedProperties.get("reportMonth"));
    }

    @Test
    @DisplayName("Should handle exception when retrieving data")
    void shouldHandleExceptionWhenRetrievingData() {
        // Arrange
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        when(spendingInsightProvider.lookup(previousMonth))
                .thenThrow(new RuntimeException("Test exception"));

        scheduler.analyzeMonthlySpendingPatterns();

        // Assert
        // Verify that no email was sent
        verify(mailDaemon, never()).send(anyString(), anyString(), any(Properties.class));
    }
}
