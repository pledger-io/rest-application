rootProject.name="Pledger"

pluginManagement {
    plugins {
        id("java")
        id("io.micronaut.library").version("4.5.4")
        id("io.micronaut.application").version("4.5.4")
        id("io.freefair.lombok").version("8.14")
        id("org.sonarqube").version("6.2.0.5505")
        id("org.openapi.generator").version("7.13.0")
        id("com.diffplug.spotless").version("7.0.4")

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
            library("otp", "dev.samstevens.totp", "totp").version("1.7.1")
            library("camunda", "org.camunda.bpm", "camunda-engine").version("7.23.0")
            library("bouncy", "org.bouncycastle", "bcprov-jdk18on").version("1.81")
            library("bcpkix", "org.bouncycastle", "bcpkix-jdk18on").version("1.81")
            library("bcrypt", "at.favre.lib", "bcrypt").version("0.10.2")
            library("csv", "com.opencsv", "opencsv").version("5.11.2")

            // testing dependencies
            version("junit.version", "5.13.3")
            library("junit.jupiter", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit.version")
            library("assertj","org.assertj", "assertj-core").version("3.27.3")
            library("mockito","org.mockito", "mockito-core").version("5.18.0")
            bundle("junit", listOf("junit.jupiter", "assertj", "mockito"))
        }

        create("mn") {
            val micronautVersion: String by settings
            from("io.micronaut.platform:micronaut-platform:${micronautVersion}")
        }

        create("llm") {
            val langchain4jVersion: String = "1.0.0-beta3"
            library("core", "dev.langchain4j", "langchain4j").version(langchain4jVersion)
            library("retriever-sql", "dev.langchain4j", "langchain4j-pgvector").version(langchain4jVersion)
            library("store", "dev.langchain4j", "langchain4j-embeddings-all-minilm-l6-v2").version(langchain4jVersion)
            library("model-openai", "dev.langchain4j", "langchain4j-open-ai").version(langchain4jVersion)
            library("model-ollama", "dev.langchain4j", "langchain4j-ollama").version(langchain4jVersion)

            bundle("embeddings", listOf("core", "store", "retriever-sql"))
            bundle("langchain4j", listOf("core", "retriever-sql", "store", "model-openai", "model-ollama"))
        }
    }
}

include(
    "core",
    "domain",
    "transaction-importer:transaction-importer-api",
    "transaction-importer:transaction-importer-csv",
    "learning:learning-module",
    "learning:learning-module-rules",
    "learning:learning-module-llm",
    "learning:learning-module-spending-patterns",
    "bpmn-process",
    "jpa-repository",
    "fintrack-api")
