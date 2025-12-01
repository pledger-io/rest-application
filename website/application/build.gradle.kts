plugins {
    id("io.micronaut.application")
    id("io.micronaut.openapi")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")

    processing {
        incremental(true)
    }
}


dependencies {
    annotationProcessor(mn.micronaut.openapi.asProvider())
    compileOnly(mn.micronaut.openapi.annotations)

    // Security setup
    implementation(mn.micronaut.security.annotations)
    implementation(mn.micronaut.security)

    implementation(mn.micronaut.serde.api)
    implementation(mn.validation)

    implementation(mn.micronaut.micrometer.core)
    implementation(mn.micronaut.micrometer.registry.prometheus)
    implementation(mn.hibernate.micrometer)

    // Email dependencies
    implementation(mn.micronaut.email.javamail)
    implementation(mn.micronaut.email.template)
    implementation(mn.micronaut.views.velocity)

    // Http Server
    implementation(mn.micronaut.http.server.jetty)
    implementation(mn.micronaut.http.validation)
    implementation(mn.micronaut.hibernate.validator)

    implementation(project(":core"))
    implementation(project(":domain"))

    // Web layer dependencies
    implementation(project(":website:system-api"))
    implementation(project(":website:runtime-api"))
    implementation(project(":website:budget-api"))
    implementation(project(":website:rest-api"))
    implementation(project(":website:importer-api"))
    implementation(project(":website:learning-rule-api"))

    runtimeOnly(mn.logback.classic)
    runtimeOnly(mn.micronaut.serde.jackson)
    runtimeOnly(mn.micronaut.jackson.databind)
    runtimeOnly(mn.eclipse.angus)

    // Contains the health checker
    implementation(mn.micronaut.management)

    implementation(project(":jpa-repository"))
    implementation(project(":bpmn-process"))

    // Libraries for running analysis and transaction corrections
    implementation(project(":learning:learning-module"))
    implementation(project(":learning:learning-module-rules"))
    implementation(project(":learning:learning-module-llm"))
    implementation(project(":learning:learning-module-spending-patterns"))

    implementation(project(":transaction-importer:transaction-importer-api"))
    implementation(project(":transaction-importer:transaction-importer-csv"))
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from("../budget-api/src/contract") {
        into("META-INF/swagger")
        exclude("*-api.yaml")
    }
    from("../importer-api/src/contract") {
        into("META-INF/swagger")
        exclude("*-api.yaml")
    }
    from("../system-api/src/contract") {
        into("META-INF/swagger")
        exclude("*-api.yaml")
    }
    from("../runtime-api/src/contract") {
        into("META-INF/swagger")
        exclude("*-api.yaml")
    }
    from("../learning-rule-api/src/contract") {
        into("META-INF/swagger")
        exclude("*-api.yaml")
    }
    from("../rest-api/src/contract") {
        into("META-INF/swagger")
        exclude("*-api.yaml")
    }

    filesMatching("**/micronaut-banner.txt") {
        filter { line ->
            var updated = line.replace("\${application.version}", project.version.toString())
            updated.replace("\${micronaut.version}", properties.get("micronautVersion").toString())
        }
    }
}
