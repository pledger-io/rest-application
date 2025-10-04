plugins {
    id("io.micronaut.application")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")
}

dependencies {
    // Security setup
    implementation(mn.micronaut.security.annotations)
    implementation(mn.micronaut.security)

    implementation(mn.micronaut.serde.api)
    implementation(mn.validation)

    // Email dependencies
    implementation(mn.micronaut.email.javamail)
    implementation(mn.micronaut.email.template)
    implementation(mn.micronaut.views.velocity)

    // Http Server
    implementation(mn.micronaut.http.server.jetty)
    implementation(mn.micronaut.http.validation)
    implementation(mn.micronaut.openapi.adoc)

    implementation(project(":core"))
    implementation(project(":domain"))

    // Web layer dependencies
    implementation(project(":website:system-api"))
    implementation(project(":website:runtime-api"))
    implementation(project(":website:budget-api"))
    implementation(project(":website:rest-api"))
    implementation(project(":website:importer-api"))

    runtimeOnly(mn.logback.classic)
    runtimeOnly(mn.micronaut.serde.jackson)
    runtimeOnly(mn.micronaut.jackson.databind)

    // Contains the health checker
    implementation(mn.micronaut.management)

    runtimeOnly(project(":jpa-repository"))
    runtimeOnly(project(":bpmn-process"))

    // Libraries for running analysis and transaction corrections
    runtimeOnly(project(":learning:learning-module"))
    runtimeOnly(project(":learning:learning-module-rules"))
    runtimeOnly(project(":learning:learning-module-llm"))
    runtimeOnly(project(":learning:learning-module-spending-patterns"))

    runtimeOnly(project(":transaction-importer:transaction-importer-api"))
    runtimeOnly(project(":transaction-importer:transaction-importer-csv"))
}

tasks.processResources {
    filesMatching("**/micronaut-banner.txt") {
        filter { line ->
            var updated = line.replace("\${application.version}", project.version.toString())
            updated.replace("\${micronaut.version}", properties.get("micronautVersion").toString())
        }
    }
}
