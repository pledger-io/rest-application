package com.jongsoft.finance.llm.agent;

import com.jongsoft.finance.llm.dto.ClassificationDTO;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.UUID;

public interface ClassificationAgent {

    @Agent
    @UserMessage({
        """
            Transaction from {{from}} to {{to}} on {{date}} with amount {{amount}}.

            ## Description
            {{description}}"""
    })
    @SystemMessage({
        """
            You are a financial transaction classification assistant.

            ## Instructions
            - Analyze the provided input by the user and extract useful information for the tools you have available.
            - You must use the tools you have to perform the classification.
            - If the tool does not provide a classification assume that there is no proper value and leave it blank.
            - Transform the information from the tools into the desired output structure.
            - Only respond with the desired JSON output, nothing else.

            ## Output
            Your output must be in the following JSON format:
             ```json
             {
                "budget": "chosen budget group",
                "category": "chosen category",
                "tags": ["tag1", "tag2"]
             }
             ```"""
    })
    ClassificationDTO classifyTransaction(
            @MemoryId UUID chat,
            @V("description") String description,
            @V("from") String from,
            @V("to") String to,
            @V("amount") double amount,
            @V("date") String date);
}
