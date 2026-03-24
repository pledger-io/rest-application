package com.jongsoft.finance.project.adapter.rest;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@DisplayName("Regression - Projects")
public class ProjectTest extends RestTestSetup {

    @Test
    @DisplayName("Create, update, fetch, list and archive a project")
    void projectLifecycle(PledgerContext context, PledgerRequests requests) {
        context.withUser("project-crud@account.local");
        requests.authenticate("project-crud@account.local");

        var clientId =
                requests.createClient(
                                Map.of(
                                        "name", "Acme Corp",
                                        "email", "billing@acme.test"))
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("id");

        var projectId =
                requests.createProject(
                                Map.of(
                                        "name", "Website build",
                                        "description", "Corporate site",
                                        "clientId", clientId,
                                        "startDate", LocalDate.now().toString(),
                                        "endDate", LocalDate.now().plusMonths(3).toString(),
                                        "billable", true))
                        .statusCode(201)
                        .body("name", equalTo("Website build"))
                        .body("billable", equalTo(true))
                        .body("client.id", equalTo((int) clientId))
                        .extract()
                        .jsonPath()
                        .getLong("id");

        requests.fetchProject(projectId)
                .statusCode(200)
                .body("name", equalTo("Website build"))
                .body("billable", equalTo(true));

        requests.updateProject(
                        projectId,
                        Map.of(
                                "name", "Website rebuild",
                                "description", "New stack",
                                "clientId", clientId,
                                "startDate", LocalDate.now().toString(),
                                "endDate", LocalDate.now().plusMonths(6).toString(),
                                "billable", true))
                .statusCode(200)
                .body("name", equalTo("Website rebuild"))
                .body("billable", equalTo(true));

        requests.createProject(
                        Map.of(
                                "name", "Internal notes",
                                "clientId", clientId,
                                "billable", false))
                .statusCode(201);

        requests.findProjects(null, clientId, true)
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Website rebuild"));

        requests.findProjects(null, clientId, null).statusCode(200).body("$", hasSize(2));

        requests.archiveProject(projectId).statusCode(204);

        requests.fetchProject(projectId).statusCode(404).body("message", equalTo("Project is not found"));
    }

    @Test
    @DisplayName("Fetch non-existing project")
    void fetchMissingProject(PledgerContext context, PledgerRequests requests) {
        context.withUser("project-missing@account.local");
        requests.authenticate("project-missing@account.local");

        requests.fetchProject(9_999_999L)
                .statusCode(404)
                .body("message", equalTo("Project is not found"));
    }
}
