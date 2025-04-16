package com.jongsoft.finance.llm.feature;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonParseException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.llm.agent.TransactionExtractorAgent;
import com.jongsoft.finance.llm.dto.AccountDTO;
import com.jongsoft.finance.llm.dto.TransactionDTO;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.lang.Control;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@EnabledIfEnvironmentVariable(named = "AI_ENABLED", matches = "true")
class TransactionExtractorAgentIT extends AiBase {

    private static final Logger log = LoggerFactory.getLogger(TransactionExtractorAgentIT.class);
    @Inject
    private TransactionExtractorAgent transactionExtractorAgent;

    @Inject
    private AccountProvider accountProvider;

    @Test
    void testExtractTransaction_en() throws IOException {
        testExtractionForLanguage("en");
    }

    private void testExtractionForLanguage(String language) throws IOException {
        var inputStream = getClass().getResourceAsStream("/%s/extractor-data.json".formatted(language));
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        List<ExtractorTestCase> testCases = objectMapper.readValue(inputStream, new TypeReference<>() {});

        for (var testCase : testCases) {
            prepareAccountMock(testCase.expected().fromAccount());
            prepareAccountMock(testCase.expected().toAccount());
            try {
                var extractedTransaction = transactionExtractorAgent.extractTransaction(UUID.randomUUID(), testCase.input());
                var expectedTransaction = testCase.expected();

                try {
                    Assertions.assertThat(extractedTransaction)
                            .as(testCase.name())
                            .isNotNull()
                            .isEqualTo(expectedTransaction);
                } catch (AssertionError e) {
                    System.out.println(testCase.name());
                    System.out.println("Expected: " + objectMapper.writeValueAsString(expectedTransaction));
                    System.out.println("Actual:   " + objectMapper.writeValueAsString(extractedTransaction));
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
        Mockito.when(accountProvider.synonymOf(accountDTO.name())).thenReturn(
                Control.Option(Account.builder()
                        .id(1L)
                        .name(accountDTO.name())
                        .type("CREDITOR")
                        .build()));
    }

    public record ExtractorTestCase(String input, String name, TransactionDTO expected) {}

}
