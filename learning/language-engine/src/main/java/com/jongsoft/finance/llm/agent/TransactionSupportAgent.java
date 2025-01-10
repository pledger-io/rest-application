package com.jongsoft.finance.llm.agent;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public interface TransactionSupportAgent {

    @SystemMessage("""
        You are a classification machine that will only respond with the following format:
        {
            "budget": "chosen budget",
            "category": "chosen category",
            "tags": ["chosen tag1", "chosen tag2", "chosen tag3"]
        }
        
        The budget, category and tags selected should match the description provided.
        Do not give any other feedback other than the desired format.""")
    @UserMessage({
            """
               You can choose one of the following budgets: {{budgets}}.
               You can choose one of the following categories: {{categories}}.
               You can choose multiple of the following tags: {{tags}}.
               If no budget or categories match then provide an empty string as value.
               Given the following description: {{description}}, can you classify the payment?"""
    })
    AiMessage classify(
            @V("budgets") List<String> budgets,
            @V("categories") List<String> categories,
            @V("tags") List<String> tags,
            @V("description") String question);
}
