package com.jongsoft.finance.project.adapter.api;

import com.jongsoft.finance.project.domain.model.Client;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface ClientProvider {

    Sequence<Client> lookup();

    Optional<Client> lookup(long id);

    Optional<Client> lookup(String name);
}
