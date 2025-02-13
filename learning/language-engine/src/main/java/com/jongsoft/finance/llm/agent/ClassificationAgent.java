package com.jongsoft.finance.llm.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ClassificationAgent {

    @SystemMessage("""
            You are a classification machine that will only respond with the following format:
            {
                "budget": "chosen budget",
                "category": "chosen category",
                "tags": ["chosen tag1", "chosen tag2", "chosen tag3"]
            }
            
            Do not give any other feedback other than the desired format.""")
    @UserMessage({
            "Classify a transaction on {{date}} from account {{from}} to account {{to}} of {{amount}} and with description {{description}}."
    })
    AiClassification classify(
            @MemoryId long chat,
            @V("description") String description,
            @V("from") String from,
            @V("to") String to,
            @V("amount") double amount,
            @V("date") String date);

    @UserMessage({
            "Classify a transaction on {{date}} from account {{from}} to account {{to}} of {{amount}} and with description {{description}}.",
            "You must pick a budget from the response of the list of known budgets, everything else is wrong.",
            "Your response must **only** contain the chosen budget name in plain text and nothing else. Do not add any explanation, formatting, or extra words."
    })
    String determineBudget(
            @MemoryId long chat,
            @V("description") String description,
            @V("from") String from,
            @V("to") String to,
            @V("amount") double amount,
            @V("date") String date);

    @UserMessage({
            "Classify a transaction on {{date}} from account {{from}} to account {{to}} of {{amount}} and with description {{description}}.",
            "You must pick a category from the response of the list of known categories, everything else is wrong.",
            "Your response must **only** contain the chosen category name in plain text and nothing else. Do not add any explanation, formatting, or extra words."
    })
    String determineCategory(
            @MemoryId long chat,
            @V("description") String description,
            @V("from") String from,
            @V("to") String to,
            @V("amount") double amount,
            @V("date") String date);
}
