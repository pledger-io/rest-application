micronaut {
    testRuntime("junit5")
}

java {
    modularity.inferModulePath.set(true)
}

tasks.withType<Test> {
    jvmArgs = listOf("--add-modules", "jdk.incubator.vector", "--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("--add-modules", "jdk.incubator.vector")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation(libs.lang)
    implementation(llm.bundles.langchain4j)
    runtimeOnly(mn.snakeyaml)

    testRuntimeOnly(mn.logback.classic)
    testImplementation(libs.bundles.junit)
}