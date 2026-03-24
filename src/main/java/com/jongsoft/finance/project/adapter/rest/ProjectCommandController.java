package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.project.adapter.api.ClientProvider;
import com.jongsoft.finance.project.adapter.api.ProjectProvider;
import com.jongsoft.finance.project.domain.model.Project;
import com.jongsoft.finance.rest.ProjectCommandApi;
import com.jongsoft.finance.rest.model.ProjectRequest;
import com.jongsoft.finance.rest.model.ProjectResponse;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ProjectCommandController implements ProjectCommandApi {

    private final Logger logger;
    private final ProjectProvider projectProvider;
    private final ClientProvider clientProvider;

    public ProjectCommandController(
            ProjectProvider projectProvider, ClientProvider clientProvider) {
        this.projectProvider = projectProvider;
        this.clientProvider = clientProvider;
        this.logger = LoggerFactory.getLogger(ProjectCommandController.class);
    }

    @Override
    public HttpResponse<@Valid ProjectResponse> createProject(ProjectRequest projectRequest) {
        logger.info("Creating project {}.", projectRequest.getName());

        var client = clientProvider
                .lookup(projectRequest.getClientId())
                .getOrThrow(
                        () -> StatusException.badRequest("Client not found", "client.not.found"));

        boolean billable = !Boolean.FALSE.equals(projectRequest.getBillable());
        Project.create(
                projectRequest.getName(),
                projectRequest.getDescription(),
                client,
                projectRequest.getStartDate(),
                projectRequest.getEndDate(),
                billable);

        var project = projectProvider
                .lookup(projectRequest.getName())
                .getOrThrow(() -> StatusException.internalError("Failed to create project"));

        return HttpResponse.created(ProjectMapper.toProjectResponse(project));
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest projectRequest) {
        logger.info("Updating project {}.", id);

        var project = locateByIdOrThrow(id);
        boolean billable = !Boolean.FALSE.equals(projectRequest.getBillable());
        project.update(
                projectRequest.getName(),
                projectRequest.getDescription(),
                projectRequest.getStartDate(),
                projectRequest.getEndDate(),
                billable);

        return ProjectMapper.toProjectResponse(project);
    }

    @Override
    public HttpResponse<Void> archiveProjectById(Long id) {
        logger.info("Archiving project {}.", id);

        locateByIdOrThrow(id).archive();
        return HttpResponse.noContent();
    }

    private Project locateByIdOrThrow(Long id) {
        return projectProvider
                .lookup(id)
                .getOrThrow(() -> StatusException.notFound("Project is not found"));
    }
}
