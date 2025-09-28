plugins {
    id("io.micronaut.application")
    id("io.micronaut.openapi")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")

    openapi {
        server(file("src/contract/system-api.yaml")) {
            apiPackageName = "com.jongsoft.finance.rest"
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

    // JWT security dependencies
    implementation(mn.micronaut.security)
    implementation(mn.micronaut.security.jwt)
    implementation(libs.bouncy)
    implementation(libs.bcpkix)
    implementation(libs.bcrypt)

    implementation(mn.micronaut.serde.api)
    implementation(mn.validation)

    implementation(mn.micronaut.email.javamail)

    implementation(libs.lang)
    implementation(libs.otp)

    implementation(project(":core"))
    implementation(project(":domain"))

    // Needed for running the tests
    testRuntimeOnly(mn.micronaut.serde.jackson)
    testRuntimeOnly(mn.micronaut.jackson.databind)
    testRuntimeOnly(mn.logback.classic)
    testRuntimeOnly(project(":bpmn-process"))
    testRuntimeOnly(project(":jpa-repository"))
    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(mn.micronaut.test.junit5)
    testImplementation(libs.bundles.junit)
}
