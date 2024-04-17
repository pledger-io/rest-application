plugins {
    id("io.micronaut.application")
}

application {
    mainClass.set("com.jongsoft.finance.Application")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")
}

dependencies {
    annotationProcessor(mn.micronaut.openapi.asProvider())
    annotationProcessor(mn.micronaut.http.validation)
    annotationProcessor(mn.micronaut.validation.processor)
    annotationProcessor(mn.micronaut.inject.java)

    implementation(libs.camunda)
    implementation(mn.swagger.annotations)

    implementation(mn.micronaut.validation)
    implementation(mn.micronaut.security.annotations)
    implementation(mn.micronaut.security.jwt)
    implementation(mn.micronaut.http.server.jetty)
    implementation(mn.micronaut.http.validation)
    implementation(mn.micronaut.email.template)
    implementation(mn.micronaut.views.velocity)

    // Email dependencies
    implementation(mn.micronaut.email.postmark)
    implementation(mn.micronaut.email.javamail)

    implementation(libs.bcrypt)
    implementation(libs.bouncy)
    implementation(libs.bcpkix)
    implementation(libs.otp)
    implementation(libs.lang)

    // Contains the health checker
    implementation(mn.micronaut.management)

    // Investigate if this can be swapped for micronaut serde
    implementation(mn.micronaut.jackson.databind)
    implementation(mn.micronaut.serde.jackson)

    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":jpa-repository"))
    implementation(project(":rule-engine"))
    implementation(project(":bpmn-process"))
    implementation(project(":transaction-importer:transaction-importer-api"))
    implementation(project(":transaction-importer:transaction-importer-csv"))

    // needed for application.yml
    runtimeOnly(mn.snakeyaml)
    runtimeOnly(mn.eclipse.angus)
    runtimeOnly(mn.postmark)

    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(mn.micronaut.test.junit5)
    testImplementation(libs.bundles.junit)
}
