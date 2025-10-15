micronaut {
    testRuntime("junit5")
}

java {
    modularity.inferModulePath.set(true)
}

dependencies {
    annotationProcessor(mn.micronaut.data.processor)

    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":learning:learning-module"))

    implementation(mn.micronaut.context)
    implementation(mn.micronaut.data.tx)
    implementation(mn.micronaut.micrometer.core)

    implementation(libs.lang)
    implementation(llm.bundles.langchain4j)
    runtimeOnly(mn.snakeyaml)

    testImplementation(libs.csv)
    testImplementation(mn.jackson.datatype.jsr310)
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(mn.logback.classic)
    testRuntimeOnly(mn.micronaut.data.tx.hibernate)
}
