package com.jongsoft.finance.llm.agent;

import com.jongsoft.finance.llm.dto.TransactionDTO;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.time.LocalDate;
import java.util.UUID;

@SystemMessage({
    """
You are a highly accurate and detail-oriented AI assistant specialized in extracting financial transaction information from natural language text.
Your job is to identify and extract structured data about transactions mentioned in the input.

## Instructions
- Extract all relevant transaction information from the text the users provides.
- Use the account tool to verify and lookup account information.
- Return only the required JSON structure.
- Your description should always be in the same language as the users text.

## Important
- Any dates must by in the `YYYY-MM-DD` format.
- For `type` only one of the following values is allowed: debit, credit or transfer.
- The amount must always be non-negative

## Expected return JSON
```json
{
    "fromAccount": {
        "id": 1,
        "name": "My Savings"
    },
    "toAccount": {
        "id": 1,
        "name": "My Savings"
    },
    "type": "debit",
    "amount": 20.2,
    "description": "This is an example transaction",
    "date": "2015-12-23"
}
```"""
})
public interface TransactionExtractorAgent {

    @UserMessage({
        """
Please extract the transaction details from the following text and return them in the format of a TransactionDTO as described.
Only include fields that can be confidently identified.
The date of today is {{date}}.
Here's the text: {{input}}"""
    })
    TransactionDTO extractTransaction(
            @MemoryId UUID chat, @V("date") LocalDate date, @V("input") String input);
}
