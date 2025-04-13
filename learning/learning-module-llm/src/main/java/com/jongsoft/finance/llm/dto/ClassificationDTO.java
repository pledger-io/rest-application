package com.jongsoft.finance.llm.dto;

import java.util.List;

public record ClassificationDTO(String category, String subCategory, List<String> tags) {
}
