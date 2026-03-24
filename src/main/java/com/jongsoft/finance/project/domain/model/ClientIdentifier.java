package com.jongsoft.finance.project.domain.model;

import com.jongsoft.finance.core.value.Identifier;

/**
 * Identifies a client by primary key, analogous to how {@link com.jongsoft.finance.core.value.UserIdentifier}
 * identifies the owning user on domain aggregates such as {@link com.jongsoft.finance.banking.domain.model.Account}.
 */
public record ClientIdentifier(Long id) implements Identifier {}
