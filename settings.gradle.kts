rootProject.name="pledger-io"

pluginManagement {
    plugins {
        id("java")
        id("io.micronaut.library").version("4.6.2")
        id("io.micronaut.application").version("4.6.2")
        id("org.sonarqube").version("7.2.2.6593")
        id("com.diffplug.spotless").version("8.2.1")
        id("io.micronaut.openapi").version("4.6.2")

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
            library("bouncy", "org.bouncycastle", "bcprov-jdk18on").version("1.83")
            library("bcpkix", "org.bouncycastle", "bcpkix-jdk18on").version("1.83")
            library("bcrypt", "at.favre.lib", "bcrypt").version("0.10.2")
            library("csv", "com.opencsv", "opencsv").version("5.12.0")
            library("archunit", "com.tngtech.archunit", "archunit-junit5").version("1.4.1")
            library("awaitly", "org.awaitility", "awaitility").version("4.3.0")
        }

        create("mn") {
            from("io.micronaut.platform:micronaut-platform:4.10.9")
        }

        create("llm") {
            val langchain4jVersion: String = "1.11.0"
            val betaVersion: String = "$langchain4jVersion-beta19"
            library("core", "dev.langchain4j", "langchain4j").version(langchain4jVersion)
            library("retriever-sql", "dev.langchain4j", "langchain4j-pgvector").version(betaVersion)
            library("store", "dev.langchain4j", "langchain4j-embeddings-all-minilm-l6-v2").version(betaVersion)
            library("agentic", "dev.langchain4j", "langchain4j-agentic").version(betaVersion)
            library("model-openai", "dev.langchain4j", "langchain4j-open-ai").version(langchain4jVersion)
            library("model-ollama", "dev.langchain4j", "langchain4j-ollama").version(langchain4jVersion)

            bundle("embeddings", listOf("core", "store", "retriever-sql"))
            bundle("langchain4j", listOf("core", "retriever-sql", "store", "agentic", "model-openai", "model-ollama"))
        }
    }
}
