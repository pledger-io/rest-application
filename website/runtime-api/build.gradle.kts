plugins {
    id("io.micronaut.library")
    id("io.micronaut.openapi")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")

    openapi {
        server(file("src/contract/runtime-api.yaml")) {
            apiPackageName = "com.jongsoft.finance.rest"
            modelPackageName = "com.jongsoft.finance.rest.model.runtime"
            useAuth = true
            useReactive = false
            generatedAnnotation = false
        }
    }
}

dependencies {
    implementation(mn.micronaut.http.validation)
    implementation(mn.micronaut.security.annotations)
    implementation(mn.micronaut.security)

    implementation(mn.micronaut.serde.api)
    implementation(mn.validation)

    implementation(libs.camunda)

    implementation(project(":core"))
    implementation(project(":domain"))

    testRuntimeOnly(mn.micronaut.serde.jackson)
    testRuntimeOnly(mn.micronaut.jackson.databind)
    testRuntimeOnly(mn.logback.classic)
    testRuntimeOnly(project(":bpmn-process"))
    testRuntimeOnly(project(":jpa-repository"))
    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(mn.micronaut.test.junit5)
    testImplementation(libs.bundles.junit)
}
