plugins {
    id("io.micronaut.library")
    id("io.micronaut.openapi")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")

    openapi {
        server("importer", file("src/contract/importer-api.yaml")) {
            apiPackageName = "com.jongsoft.finance.rest"
            modelPackageName = "com.jongsoft.finance.rest.model"
            useAuth = true
            useReactive = false
            generatedAnnotation = false

            importMapping = mapOf(
                "ExternalErrorResponse" to "io.micronaut.http.hateoas.JsonError",
            )
            typeMapping = mapOf(
                "json-error-response" to "ExternalErrorResponse"
            )
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
}
