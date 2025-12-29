package com.jongsoft.finance.llm.dto;

import java.util.List;

public record ClassificationDTO(String budget, String category, List<String> tags) {}
