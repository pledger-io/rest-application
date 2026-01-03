plugins {
    id("io.micronaut.library")
    id("com.diffplug.spotless")
    id("maven-publish")
    id("java")

    id("org.sonarqube")
    id("jacoco")
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
    runtime("netty")
}

spotless {
    java {
        target("src/main/java/**")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        palantirJavaFormat("2.75.0").style("AOSP")
    }
}

dependencies {
    implementation(mn.micronaut.runtime)
    implementation(mn.micronaut.data.jpa)

    implementation(mn.log4j)

    implementation(libs.lang)

    testImplementation(libs.bundles.junit)
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
