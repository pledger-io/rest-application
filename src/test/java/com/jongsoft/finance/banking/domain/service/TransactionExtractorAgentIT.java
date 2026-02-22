package com.jongsoft.finance.banking.domain.service;

import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonParseException;
import com.jongsoft.finance.AiBase;
import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.service.ai.TransactionDTO;
import com.jongsoft.finance.banking.domain.service.ai.TransactionExtractorAgent;
import com.jongsoft.finance.banking.domain.service.tools.AccountDTO;
import com.jongsoft.lang.Control;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@EnabledIfEnvironmentVariable(named = "AI_ENGINE", matches = "ollama")
class TransactionExtractorAgentIT extends AiBase {

    private static final Logger log = LoggerFactory.getLogger(TransactionExtractorAgentIT.class);

    @Inject
    private TransactionExtractorAgent transactionExtractorAgent;

    @Inject
    private AccountProvider accountProvider;

    @MockBean
    @Replaces
    AccountProvider mockAccountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @Test
    void testExtractTransaction_en() throws IOException {
        testExtractionForLanguage("en");
    }

    private void testExtractionForLanguage(String language) throws IOException {
        var inputStream = getClass()
                .getResourceAsStream("/banking/%s/extractor-data.json".formatted(language));
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        List<ExtractorTestCase> testCases =
                objectMapper.readValue(inputStream, new TypeReference<>() {});

        for (var testCase : testCases) {
            prepareAccountMock(testCase.expected().fromAccount());
            prepareAccountMock(testCase.expected().toAccount());
            try {
                var extractedTransaction = transactionExtractorAgent.extractTransaction(
                        UUID.randomUUID(), LocalDate.now(), testCase.input());
                var expectedTransaction = testCase.expected();

                try {
                    Assertions.assertThat(extractedTransaction)
                            .as(testCase.name())
                            .isNotNull()
                            .isEqualTo(expectedTransaction);
                } catch (AssertionError e) {
                    System.out.println(testCase.name());
                    System.out.println(
                            "Expected: " + objectMapper.writeValueAsString(expectedTransaction));
                    System.out.println(
                            "Actual:   " + objectMapper.writeValueAsString(extractedTransaction));
                }
            } catch (JsonParseException e) {
                log.error("Failed to parse JSON for: {}\n", testCase.input(), e);
            }
        }
    }

    private void prepareAccountMock(AccountDTO accountDTO) {
        if (accountDTO == null || accountDTO.name().isBlank()) {
            return;
        }

        var account = Mockito.mock(Account.class);
        doReturn(1L).when(account).getId();
        doReturn(accountDTO.name()).when(account).getName();
        doReturn("CREDITOR").when(account).getType();
        Mockito.when(accountProvider.synonymOf(accountDTO.name()))
                .thenReturn(Control.Option(account));
    }

    public record ExtractorTestCase(String input, String name, TransactionDTO expected) {}
}
