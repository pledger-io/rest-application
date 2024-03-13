rootProject.name="Pledger"

pluginManagement {
    plugins {
        id("java")
        id("io.micronaut.library").version("4.3.4")
        id("io.micronaut.application").version("4.3.4")
        id("io.freefair.lombok").version("8.4")

        id("signing")
        id("maven-publish")
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage") // It's gradle, any of their APIs can be considered unstable
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            library("lang", "com.jongsoft.lang", "language").version("1.1.6")
            library("lang.xml", "com.jongsoft.lang", "language-fasterxml").version("1.1.0")
            library("otp", "org.jboss.aerogear", "aerogear-otp-java").version("1.0.0")
            library("camunda", "org.camunda.bpm", "camunda-engine").version("7.20.0")
            library("bouncy", "org.bouncycastle", "bcprov-jdk15on").version("1.70")
            library("bcpkix", "org.bouncycastle", "bcpkix-jdk15on").version("1.70")
            library("csv", "com.opencsv", "opencsv").version("5.7.1")

            // testing dependencies
            version("junit.version", "5.10.2")
            library("junit.jupiter", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit.version")
            library("assertj","org.assertj", "assertj-core").version("3.22.0")
            library("mockito","org.mockito", "mockito-core").version("4.5.0")
            bundle("junit", listOf("junit.jupiter", "assertj", "mockito"))
        }

        create("mn") {
            val micronautVersion: String by settings
            from("io.micronaut.platform:micronaut-platform:${micronautVersion}")
        }
    }
}

include("core", "domain", "rule-engine", "bpmn-process", "jpa-repository", "fintrack-api")
