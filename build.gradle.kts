plugins {
    id("io.micronaut.library") apply false
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

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.micronaut.library")
    apply(plugin = "maven-publish")
    apply(plugin = "jacoco")

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        reports {
            xml.required = true
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "com.jongsoft.finance"
                version = "3.3.0-SNAPSHOT"
                from(components["java"])
            }
        }

        repositories {
            maven {
                url = uri("https://maven.pkg.github.com/pledger-io/central")
                credentials {
                    username = System.getenv("NEXUS_USER")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }
}