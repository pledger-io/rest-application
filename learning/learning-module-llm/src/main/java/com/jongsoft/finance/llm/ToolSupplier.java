package com.jongsoft.finance.llm;

@FunctionalInterface
public interface ToolSupplier {
    Object[] getTools();
}
