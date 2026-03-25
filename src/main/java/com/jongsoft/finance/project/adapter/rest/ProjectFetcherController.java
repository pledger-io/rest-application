package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.project.adapter.api.ProjectProvider;
import com.jongsoft.finance.rest.ProjectFetcherApi;
import com.jongsoft.finance.rest.model.ProjectResponse;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
class ProjectFetcherController implements ProjectFetcherApi {

    private final Logger logger;
    private final ProjectProvider projectProvider;

    public ProjectFetcherController(ProjectProvider projectProvider) {
        this.projectProvider = projectProvider;
        this.logger = LoggerFactory.getLogger(ProjectFetcherController.class);
    }

    @Override
    public List<ProjectResponse> findProjects(String name, Long clientId, Boolean billableOnly) {
        logger.info("Fetching projects with filters.");
        return projectProvider
                .lookup(name, clientId, billableOnly)
                .map(ProjectMapper::toProjectResponse)
                .toJava();
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        logger.info("Fetching project {}.", id);
        return projectProvider
                .lookup(id)
                .map(ProjectMapper::toProjectResponse)
                .getOrThrow(() -> StatusException.notFound("Project is not found"));
    }
}
