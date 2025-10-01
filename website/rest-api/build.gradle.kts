plugins {
    id("io.micronaut.application")
    id("io.micronaut.openapi")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")

    openapi {
        server(file("src/contract/rest-api.yaml")) {
            apiPackageName = "com.jongsoft.finance.rest.api"
            modelPackageName = "com.jongsoft.finance.rest.model"
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

    implementation(libs.lang)

    implementation(project(":core"))
    implementation(project(":domain"))

    // Needed for running the tests
    testRuntimeOnly(mn.micronaut.serde.jackson)
    testRuntimeOnly(mn.micronaut.jackson.databind)
    testRuntimeOnly(mn.logback.classic)

    testRuntimeOnly(project(":jpa-repository"))

    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(libs.bundles.junit)
}
