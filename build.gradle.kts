plugins {
    id("io.micronaut.library") apply false
    id("maven-publish")
    id("java")
    id("org.sonarqube")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.micronaut.library")
    apply(plugin = "maven-publish")

    sonar {
        properties {
            property("sonar.projectKey", "FinTrack:API")
            property("sonar.organization", "jongsoft")
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
                uri("https://maven.pkg.github.com/pledger-io/central")
                credentials {
                    username = System.getenv("NEXUS_USER")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }
}