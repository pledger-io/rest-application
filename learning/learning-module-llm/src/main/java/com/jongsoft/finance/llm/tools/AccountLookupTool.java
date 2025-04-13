package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.account.Account;
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
    private static final AccountDTO UNKNOWN_ACCOUNT = new AccountDTO("Unknown", "");

    private final AccountProvider accountProvider;

    public AccountLookupTool(AccountProvider accountProvider) {
        this.accountProvider = accountProvider;
    }

    @Tool("""
            Fetches detailed account information based on the account name.
            Use this to populate the fromAccount or toAccount fields when only the account name is known.
            You have access to a tool named lookupAccount that allows you to retrieve information about a financial account by name.
            Use this tool whenever you need to resolve an account name into an AccountDTO object.
            
            Only invoke this tool when you are ready to fill in account information using a recognized name.""")
    public AccountDTO lookup(String accountName) {
        LOGGER.trace("Ai tool looking up account information for {}.", accountName);
        return accountProvider.lookup(accountName)
                .map(this::convert)
                .getOrSupply(() -> accountProvider.synonymOf(accountName)
                        .map(this::convert)
                        .getOrSupply(() -> UNKNOWN_ACCOUNT));
    }

    private AccountDTO convert(Account account) {
        return new AccountDTO(account.getName(), account.getType());
    }
}
