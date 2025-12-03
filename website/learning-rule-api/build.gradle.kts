plugins {
    id("io.micronaut.library")
    id("io.micronaut.openapi")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")

    openapi {
        server(file("src/contract/learning-rule-api.yaml")) {
            apiPackageName = "com.jongsoft.finance.rest"
            modelPackageName = "com.jongsoft.finance.rest.model.rule"
            useAuth = true
            useReactive = false
            generatedAnnotation = false

            importMapping = mapOf(
                "JsonError" to "io.micronaut.http.hateoas.JsonError",
                "RuleOperation" to "com.jongsoft.finance.core.RuleOperation",
                "RuleColumn" to "com.jongsoft.finance.core.RuleColumn",
            )
            typeMapping = mapOf(
                "json-error-response" to "JsonError",
                "operation-type" to "RuleOperation",
                "rule-column" to "RuleColumn"
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
    implementation(project(":learning:learning-module"))

    testRuntimeOnly(mn.micronaut.serde.jackson)
    testRuntimeOnly(mn.micronaut.jackson.databind)
    testRuntimeOnly(mn.logback.classic)

    testRuntimeOnly(project(":jpa-repository"))

    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(libs.bundles.junit)
    testImplementation(mn.micronaut.http.server.jetty)
}
