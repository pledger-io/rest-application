package com.jongsoft.finance.llm.dto;

import dev.langchain4j.model.output.structured.Description;

@Description("A financial account that can be used in transactions.")
public record AccountDTO(
    @Description("The unique identifier of the account.") Long id,
    @Description("The name of the account.") String name,
    @Description("The type of the account, leave blank at all times.") String type) {}
