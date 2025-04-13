micronaut {
    testRuntime("junit5")
}

java {
    modularity.inferModulePath.set(true)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":learning:learning-module"))

    implementation(mn.micronaut.context)
    implementation(libs.lang)
    implementation(llm.bundles.langchain4j)
    runtimeOnly(mn.snakeyaml)

    testImplementation(libs.csv)
    testImplementation(mn.jackson.datatype.jsr310)
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(mn.logback.classic)
}
