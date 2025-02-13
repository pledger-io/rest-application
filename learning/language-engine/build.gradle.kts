micronaut {
    testRuntime("junit5")
}

java {
    modularity.inferModulePath.set(true)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation(libs.lang)
    implementation(llm.bundles.langchain4j)
    runtimeOnly(mn.snakeyaml)

    testImplementation(libs.csv)
    testRuntimeOnly(mn.logback.classic)
    testImplementation(libs.bundles.junit)
}