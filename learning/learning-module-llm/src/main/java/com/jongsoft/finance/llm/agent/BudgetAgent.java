package com.jongsoft.finance.llm.agent;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;

public interface BudgetAgent {

    @Agent
    @SystemMessage(
            """
        ## Steps
        - Extract the needed information from the provided transaction description.
        - Use the provided tool to list the available budgets.
        - Select at most one budget from the list matching the transaction description.
        - Your chosen budget *must* be from the list, do not make one up yourself.

        ## Important
        - Only return the chosen budget text.""")
    @Tool(
            name = "budget-classifier",
            value =
                    """
        Determine the correct budget group for any given transaction text.""")
    String classifyBudget(String question);
}
