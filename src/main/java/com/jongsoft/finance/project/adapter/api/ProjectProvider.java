package com.jongsoft.finance.project.adapter.api;

import com.jongsoft.finance.project.domain.model.Project;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface ProjectProvider {

    Optional<Project> lookup(long id);

    Optional<Project> lookup(String name);

    Sequence<Project> lookup(String name, Long clientId, Boolean billableOnly);
}
