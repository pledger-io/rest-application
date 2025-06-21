package com.jongsoft.finance.spending.scheduler;

import com.jongsoft.finance.core.MailDaemon;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.providers.SpendingInsightProvider;
import com.jongsoft.finance.providers.SpendingPatternProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.spending.SpendingAnalyticsEnabled;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@SpendingAnalyticsEnabled
class SpendingReportScheduler {

  private final SpendingInsightProvider spendingInsightProvider;
  private final SpendingPatternProvider spendingPatternProvider;
  private final CurrentUserProvider currentUserProvider;
  private final MailDaemon mailDaemon;

  SpendingReportScheduler(
      SpendingInsightProvider spendingInsightProvider,
      SpendingPatternProvider spendingPatternProvider,
      CurrentUserProvider currentUserProvider,
      MailDaemon mailDaemon) {
    this.spendingInsightProvider = spendingInsightProvider;
    this.spendingPatternProvider = spendingPatternProvider;
    this.currentUserProvider = currentUserProvider;
    this.mailDaemon = mailDaemon;
  }

  @Scheduled(cron = "0 0 0 20 1-12 *")
  public void analyzeMonthlySpendingPatterns() {
    try {
      // Get the previous month
      YearMonth previousMonth = YearMonth.now().minusMonths(1);
      log.info("Sending out mail report for the previous month of analysis {}.", previousMonth);

      // Get the current user
      UserAccount user = currentUserProvider.currentUser();

      // Retrieve spending insights and patterns for the previous month
      var insights = spendingInsightProvider.lookup(previousMonth);
      var patterns = spendingPatternProvider.lookup(previousMonth);

      // Format the month for display
      String formattedMonth = previousMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

      // Create properties for the email template
      Properties mailProperties = new Properties();
      mailProperties.put("user", user);
      mailProperties.put("reportMonth", formattedMonth);
      mailProperties.put("insights", insights);
      mailProperties.put("patterns", patterns);

      // Send the email
      mailDaemon.send(user.getUsername().email(), "spending-report", mailProperties);

      log.info(
          "Spending report email sent to {} for {}", user.getUsername().email(), formattedMonth);
    } catch (Exception e) {
      log.error("Error sending spending report email: {}", e.getMessage(), e);
    }
  }
}
