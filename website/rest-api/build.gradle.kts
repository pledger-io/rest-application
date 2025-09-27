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

    implementation(project(":domain"))
}
