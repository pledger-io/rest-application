package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.project.domain.model.Project;
import com.jongsoft.finance.rest.model.ProjectResponse;
import com.jongsoft.finance.rest.model.ProjectResponseClient;

interface ProjectMapper {

    static ProjectResponse toProjectResponse(Project project) {
        var response = new ProjectResponse(
                project.getId(),
                project.getName(),
                new ProjectResponseClient(project.getClient().id()),
                project.isBillable());
        response.setDescription(project.getDescription());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setArchived(project.isArchived());
        return response;
    }
}
