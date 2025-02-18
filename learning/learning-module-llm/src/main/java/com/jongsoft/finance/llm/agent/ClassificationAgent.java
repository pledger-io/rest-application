package com.jongsoft.finance.llm.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;
import java.util.UUID;

@SystemMessage({
        "You are an AI assistant who specializes in classifying financial transactions for users.",
        "You should be formal and concise. Your answer must only contain asked information and nothing else. Do not add any explanation."
})
public interface ClassificationAgent {

    @UserMessage({
            "Pick the correct category for a transaction on {{date}} from account {{from}} to account {{to}} of {{amount}} and with description {{description}}.",
            "You must pick a category from the response of the list of known categories, everything else is wrong.",
            "If you cannot find a match do not answer at all.",
            "Your response must **only** contain the chosen category name in plain text and nothing else. Do not add any explanation, formatting, or extra words."
    })
    String determineCategory(
            @MemoryId UUID chat,
            @V("description") String description,
            @V("from") String from,
            @V("to") String to,
            @V("amount") double amount,
            @V("date") String date);

    @UserMessage({
            "Pick the correct subcategory a transaction on {{date}} from account {{from}} to account {{to}} of {{amount}} and with description {{description}}.",
            "You must pick a subcategory from the response of the list of known subcategories, everything else is wrong.",
            "If you cannot find a match do not answer at all.",
            "Your response must **only** contain the chosen subcategory name in plain text and nothing else. Do not add any explanation, formatting, or extra words."
    })
    String determineSubCategory(
            @MemoryId UUID chat,
            @V("description") String description,
            @V("from") String from,
            @V("to") String to,
            @V("amount") double amount,
            @V("date") String date);

    @UserMessage({
            "Pick the correct tags for a transaction on {{date}} from account {{from}} to account {{to}} of {{amount}} and with description {{description}}.",
            "You must pick zero or more tags from the response of the list of known tags, everything else is wrong.",
            "If you cannot find a match do not answer at all.",
            "Your response must **only** contain the chosen tags in plain text and nothing else. Do not add any explanation, formatting, or extra words."
    })
    List<String> determineTags(
            @MemoryId UUID chat,
            @V("description") String description,
            @V("from") String from,
            @V("to") String to,
            @V("amount") double amount,
            @V("date") String date);
}
