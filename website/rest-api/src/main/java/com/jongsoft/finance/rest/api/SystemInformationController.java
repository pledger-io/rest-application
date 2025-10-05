package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.providers.AccountTypeProvider;

import io.micronaut.http.annotation.Controller;

import jakarta.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
class SystemInformationController implements SystemInformationApi {

    private final Logger logger;
    private final AccountTypeProvider accountTypeProvider;

    SystemInformationController(AccountTypeProvider accountTypeProvider) {
        this.accountTypeProvider = accountTypeProvider;
        this.logger = LoggerFactory.getLogger(SystemInformationController.class);
    }

    @Override
    public List<@NotNull String> getAccountTypes() {
        logger.info("Fetching all account types.");
        return accountTypeProvider.lookup(false).toJava();
    }
}
