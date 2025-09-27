rootProject.name="Pledger"

pluginManagement {
    plugins {
        id("java")
        id("io.micronaut.library").version("4.5.4")
        id("io.micronaut.application").version("4.5.4")
        id("io.micronaut.openapi").version("4.5.4")
        id("io.freefair.lombok").version("8.14.2")
        id("org.sonarqube").version("6.3.1.5724")
        id("org.openapi.generator").version("7.15.0")
        id("com.diffplug.spotless").version("7.2.1")

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
            library("bouncy", "org.bouncycastle", "bcprov-jdk18on").version("1.82")
            library("bcpkix", "org.bouncycastle", "bcpkix-jdk18on").version("1.81")
            library("bcrypt", "at.favre.lib", "bcrypt").version("0.10.2")
            library("csv", "com.opencsv", "opencsv").version("5.12.0")

            // testing dependencies
            version("junit.version", "5.13.4")
            library("junit.jupiter", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit.version")
            library("assertj","org.assertj", "assertj-core").version("3.27.5")
            library("mockito","org.mockito", "mockito-core").version("5.19.0")
            bundle("junit", listOf("junit.jupiter", "assertj", "mockito"))
        }

        create("mn") {
            from("io.micronaut.platform:micronaut-platform:4.9.3")
        }

        create("llm") {
            val langchain4jVersion: String = "1.5.0"
            library("core", "dev.langchain4j", "langchain4j").version(langchain4jVersion)
            library("retriever-sql", "dev.langchain4j", "langchain4j-pgvector").version("1.3.0-beta9")
            library("store", "dev.langchain4j", "langchain4j-embeddings-all-minilm-l6-v2").version("1.3.0-beta9")
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
    "website:rest-api",
    "website:runtime-api",
    "website:importer-api",
    "website:system-api",
    "website:budget-api",
    "website:application",
    "fintrack-api")
