rootProject.name="Pledger"

pluginManagement {
    plugins {
        id("java")
        id("io.micronaut.library").version("4.4.5")
        id("io.micronaut.application").version("4.4.5")
        id("io.freefair.lombok").version("8.12.1")
        id("org.sonarqube").version("6.0.1.5171")
        id("org.openapi.generator").version("7.11.0")

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
            library("camunda", "org.camunda.bpm", "camunda-engine").version("7.22.0")
            library("bouncy", "org.bouncycastle", "bcprov-jdk18on").version("1.80")
            library("bcpkix", "org.bouncycastle", "bcpkix-jdk18on").version("1.80")
            library("bcrypt", "at.favre.lib", "bcrypt").version("0.10.2")
            library("csv", "com.opencsv", "opencsv").version("5.10")

            // testing dependencies
            version("junit.version", "5.11.4")
            library("junit.jupiter", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit.version")
            library("assertj","org.assertj", "assertj-core").version("3.27.3")
            library("mockito","org.mockito", "mockito-core").version("5.15.2")
            bundle("junit", listOf("junit.jupiter", "assertj", "mockito"))
        }

        create("mn") {
            val micronautVersion: String by settings
            from("io.micronaut.platform:micronaut-platform:${micronautVersion}")
        }

        create("llm") {
            val langchain4jVersion: String = "0.35.0"
            library("core", "dev.langchain4j", "langchain4j").version(langchain4jVersion)
            library("rag", "dev.langchain4j", "langchain4j-easy-rag").version(langchain4jVersion)
            library("store", "dev.langchain4j", "langchain4j-embeddings-all-minilm-l6-v2").version(langchain4jVersion)
            library("model-jlama", "dev.langchain4j", "langchain4j-jlama").version(langchain4jVersion)
            library("model-openai", "dev.langchain4j", "langchain4j-open-ai").version(langchain4jVersion)
            library("native", "com.github.tjake", "jlama-native").version("0.7.0")

            bundle("langchain4j", listOf("core", "rag", "store", "model-openai", "model-jlama"))
        }
    }
}

include(
    "core",
    "domain",
    "transaction-importer:transaction-importer-api",
    "transaction-importer:transaction-importer-csv",
    "learning:rule-engine",
    "learning:language-engine",
    "bpmn-process",
    "jpa-repository",
    "fintrack-api")
