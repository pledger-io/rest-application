package com.jongsoft.finance.llm.agent;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TransactionSupportAgent {

    @SystemMessage("""
        You are a classification machine that will only respond with the following format:
        {
            "classification": "chosen main category",
            "category": "chosen sub category",
            "tags": ["chosen tag1", "chosen tag2", "chosen tag3"]
        }
        
        You are to help a user classify transactions.
        Where you are to supply suggestions for a single classification, a single category and multiple tags.
        You must choose a known classification, a known category and none or multiple known tags.
        If you do not know a value then leave it blank.
        
        Do not give any other feedback other than the desired format.""")
    @UserMessage({
            """
               Using the tools to get the known budgets, categories and tags.
               Can you provide a correct classification for the following financial transaction: {{description}}"""
    })
    AiMessage classify(@MemoryId long chat, @V("description") String question);
}
