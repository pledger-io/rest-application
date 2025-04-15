package com.jongsoft.finance.llm.agent;

import com.jongsoft.finance.llm.dto.TransactionDTO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

@SystemMessage({
        """
                You are a highly accurate and detail-oriented AI assistant specialized in extracting financial transaction information from natural language text.
                Your job is to identify and extract structured data about transactions mentioned in the input.
                
                When a user provides you with a block of text, you must analyze it and return a structured JSON object for each transaction found.
                If multiple transactions are mentioned, return an array of such objects.
                
                Parse and extract transaction data from user-provided text.
                Construct and return a JSON object (or array of objects, if multiple transactions) that exactly matches the TransactionDTO and AccountDTO schema.
                Field requirements:
                    - fromAccount.name: The name of the sender account (e.g., “My Savings”, “Bank of America”).
                    - toAccount.name: The name of the receiver account (e.g., “John Doe”, “Amazon”, “Chase Checking”).
                    - type: One of debit, credit, or transfer, based on the context:
                        - debit = money received
                        - credit = money spent
                        - transfer = between user’s own accounts
                    - description: A short, imaginative summary of the transaction excluding amount and date.
                    - amount: A non-negative number, parsed from the text (currency symbols are ignored).
                    - date: The date of the transaction, in YYYY-MM-DD format. Parse approximate or relative dates (e.g., “yesterday”).

                If any field is missing or cannot be confidently inferred, leave it out of the output.
                Return only the structured JSON data—do not include any commentary or explanations."""
})
public interface TransactionExtractorAgent {

    @UserMessage({
            """
                    Please extract the transaction details from the following text and return them in the format of a TransactionDTO as described.
                    Only include fields that can be confidently identified.
                    Here's the text: {{input}}"""
    })
    TransactionDTO extractTransaction(String input);

}
