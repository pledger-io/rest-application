package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.project.domain.model.Project;
import com.jongsoft.finance.rest.model.ProjectResponse;

interface ProjectMapper {

    static ProjectResponse toProjectResponse(Project project) {
        var response = new ProjectResponse(
                project.getId(),
                project.getName(),
                ClientMapper.toClientResponse(project.getClient()),
                project.isBillable());
        response.setDescription(project.getDescription());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setArchived(project.isArchived());
        return response;
    }
}
