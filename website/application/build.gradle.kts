plugins {
    id("io.micronaut.application")
    id("io.micronaut.openapi")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")
}

dependencies {
    implementation(mn.micronaut.http.validation)
    implementation(mn.micronaut.security.annotations)
    implementation(mn.micronaut.security)

    implementation(mn.micronaut.serde.api)
    implementation(mn.validation)

    implementation(project(":website:system-api"))
    implementation(project(":website:runtime-api"))
    implementation(project(":website:budget-api"))
    implementation(project(":website:rest-api"))
}
