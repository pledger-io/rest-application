package com.jongsoft.finance.llm.dto;

import com.jongsoft.finance.core.TransactionType;
import dev.langchain4j.model.output.structured.Description;

import java.time.LocalDate;

@Description("The entity for deposits, withdrawals or transfers of money.")
public record TransactionDTO(
        @Description("The account that made the payment.")
        AccountDTO fromAccount,
        @Description("The account that received the payment.")
        AccountDTO toAccount,
        @Description("Write a short description for the transaction, excludes dates and amounts. Be creative.")
        String description,
        @Description("The date of the transaction, in YYYY-MM-DD format.")
        LocalDate date,
        @Description("The amount of the transaction, cannot be a negative number.")
        double amount,
        @Description("The type of the transaction, debit for income, credit for expenses or transfer for transfers between my own accounts.")
        TransactionType type) {
}
