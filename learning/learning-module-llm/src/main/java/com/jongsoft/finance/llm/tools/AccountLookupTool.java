package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.llm.dto.AccountDTO;
import com.jongsoft.finance.providers.AccountProvider;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

@Singleton
@AiEnabled
public class AccountLookupTool implements AiTool {
  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AccountLookupTool.class);
  private static final AccountDTO UNKNOWN_ACCOUNT = new AccountDTO(-1L, "Unknown", "");

  private final AccountProvider accountProvider;
  private final FilterFactory filterFactory;

  public AccountLookupTool(AccountProvider accountProvider, FilterFactory filterFactory) {
    this.accountProvider = accountProvider;
    this.filterFactory = filterFactory;
  }

  @Tool("Fetch account information for a given account name.")
  public AccountDTO lookup(String accountName) {
    LOGGER.debug("Ai tool looking up account information for {}.", accountName);

    var filter = filterFactory.account().name(accountName, false).page(0, 1);

    var result = accountProvider.lookup(filter).content().map(this::convert);
    if (result.isEmpty()) {
      return accountProvider
          .synonymOf(accountName)
          .map(this::convert)
          .getOrSupply(
              () -> {
                LOGGER.trace("Ai tool could not find account information for {}.", accountName);
                return UNKNOWN_ACCOUNT;
              });
    }

    return result.head();
  }

  private AccountDTO convert(Account account) {
    return new AccountDTO(account.getId(), account.getName(), account.getType());
  }
}
