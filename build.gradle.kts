plugins {
    id("io.micronaut.application")
    id("com.diffplug.spotless")
    id("maven-publish")
    id("java")

    id("org.sonarqube")
    id("jacoco")
    id("io.micronaut.openapi")
}

sonar {
    properties {
        property("sonar.projectKey", "pledger-io_rest-application")
        property("sonar.organization", "pledger-io")
    }
}

group = "com.jongsoft.finance"

micronaut {
    testRuntime("junit5")
    runtime("jetty")

    openapi {
        server(file("src/contract/pledger-api.yaml")) {
            apiPackageName = "com.jongsoft.finance.rest"
            modelPackageName = "com.jongsoft.finance.rest.model"

            useAuth = true
            useReactive = false
            generateSwaggerAnnotations = true

            importMapping = mapOf(
                "JsonError" to "io.micronaut.http.hateoas.JsonError",
                "RuleOperation" to "com.jongsoft.finance.suggestion.types.RuleOperation",
                "RuleColumn" to "com.jongsoft.finance.suggestion.types.RuleColumn",
                "ProcessVariable" to "com.jongsoft.finance.core.domain.model.ProcessVariable",
                )
            typeMapping = mapOf(
                "process-variable" to "ProcessVariable",
                "json-error-response" to "JsonError",
                "operation-type" to "RuleOperation",
                "rule-column" to "RuleColumn"
            )
        }
    }
}

spotless {
    java {
        target("src/main/java/**", "src/test/java/**/domain/jpa/**", "src/test/java/**/domain/service/**")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        palantirJavaFormat("2.75.0").style("AOSP")
    }
}

tasks.classes {
    dependsOn("spotlessApply")
}

tasks.test {
    maxParallelForks = 1
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}

tasks.processResources {
    filesMatching("**/micronaut-banner.txt") {
        filter { line ->
            var updated = line.replace("\${application.version}", project.version.toString())
            updated.replace("\${micronaut.version}", properties.get("micronautVersion").toString())
        }
    }
}

dependencies {
    implementation(mn.micronaut.runtime)

    // Rest API dependencies
    implementation(mn.micronaut.security)
    implementation(mn.micronaut.security.jwt)
    implementation(mn.micronaut.serde.api)
    implementation(mn.validation)

    implementation(mn.micronaut.http.client.jdk)

    // Database dependencies
    implementation(mn.micronaut.data.jpa)
    implementation(mn.micronaut.data.jdbc)
    implementation(mn.micronaut.data.tx.hibernate)
    implementation(mn.micronaut.hibernate.jpa)
    implementation(mn.micronaut.jdbc.hikari)
    implementation(mn.micronaut.flyway)

    // Micrometer required dependencies
    implementation(mn.micronaut.micrometer.core)
    implementation(mn.micronaut.micrometer.registry.prometheus)
    implementation(mn.hibernate.micrometer)
    implementation(mn.micronaut.management)

    // Email dependencies
    implementation(mn.micronaut.email.javamail)
    implementation(mn.micronaut.email.template)
    implementation(mn.micronaut.views.velocity)

    // Http Server
    implementation(mn.micronaut.http.server.jetty)
    implementation(mn.micronaut.http.validation)
    implementation(mn.micronaut.hibernate.validator)

    implementation(mn.log4j)

    implementation(libs.bouncy)
    implementation(libs.bcpkix)
    implementation(libs.bcrypt)

    implementation(libs.lang)
    implementation(libs.otp)

    implementation(libs.csv)

    // Machine learning dependencies
    implementation(llm.bundles.embeddings)
    implementation(llm.bundles.langchain4j)
    implementation("org.apache.commons:commons-math3:3.6.1")

    runtimeOnly(mn.micronaut.serde.jackson)
    runtimeOnly(mn.h2)
    runtimeOnly(mn.postgresql)
    runtimeOnly(mn.mysql.connector.java)
    runtimeOnly(mn.flyway.mysql)
    runtimeOnly(mn.flyway.postgresql)

    // Setup for the test suites
    testImplementation(mn.micronaut.test.junit5)
    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(libs.awaitly)
    testImplementation(mn.junit.jupiter.api)
    testImplementation(mn.mockito.core)
    testImplementation(mn.assertj.core)
    testImplementation(libs.archunit)
    testRuntimeOnly(mn.logback.classic)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.jongsoft.finance"
            version = System.getProperty("version")
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/pledger-io/rest-application")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
