plugins {
    id("io.micronaut.library") apply false
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

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "jacoco")
    apply(plugin = "com.diffplug.spotless")

    if (project.name != "fintrack-api") {
        apply(plugin = "io.micronaut.library")
    }

    tasks.check {
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        reports {
            xml.required = true
        }
    }

    spotless {
        java {
            target("src/main/java/**")
            importOrder()
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
            palantirJavaFormat("2.68.0").style("GOOGLE")
        }
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
}
